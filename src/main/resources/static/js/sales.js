/**
 * ENTERPRISE SALES INVOICE - sales.js
 * ERP-grade JavaScript for Sales Invoice workflow
 */

'use strict';

/* ============================================================
   CONSTANTS & STATE
   ============================================================ */

const CURRENCY_SYMBOL = '৳';

const State = {
  selectedCustomer: null,
  itemRowCount: 0,
  products: [],          // populated from data attribute
  customers: [],         // populated from data attribute
  pendingAction: null,   // for confirmation dialog
};

/* ============================================================
   DOM HELPERS
   ============================================================ */

const $ = (sel, ctx = document) => ctx.querySelector(sel);
const $$ = (sel, ctx = document) => [...ctx.querySelectorAll(sel)];

function numVal(el) {
  const v = parseFloat((el?.value || '0').replace(/,/g, ''));
  return isNaN(v) ? 0 : v;
}

function fmtMoney(n) {
  return CURRENCY_SYMBOL + ' ' + Number(n).toLocaleString('en-BD', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

/* ============================================================
   TOAST NOTIFICATIONS
   ============================================================ */

function showToast(type, title, msg, duration = 3500) {
  const icons = { success: 'bi-check-circle-fill', error: 'bi-x-circle-fill', warning: 'bi-exclamation-triangle-fill' };
  let toast = document.getElementById('erpToast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'erpToast';
    toast.className = 'erp-toast';
    toast.innerHTML = `
      <i class="toast-icon bi"></i>
      <div>
        <div class="toast-title"></div>
        <div class="toast-msg"></div>
      </div>`;
    document.body.appendChild(toast);
  }
  toast.className = `erp-toast ${type}`;
  toast.querySelector('.toast-icon').className = `toast-icon bi ${icons[type] || 'bi-info-circle'}`;
  toast.querySelector('.toast-title').textContent = title;
  toast.querySelector('.toast-msg').textContent = msg;
  requestAnimationFrame(() => toast.classList.add('show'));
  clearTimeout(toast._timeout);
  toast._timeout = setTimeout(() => toast.classList.remove('show'), duration);
}

/* ============================================================
   CONFIRM MODAL
   ============================================================ */

function showConfirm({ type = 'warning', icon, title, desc, confirmLabel, cancelLabel = 'Cancel', onConfirm }) {
  const overlay = document.getElementById('confirmModal');
  overlay.querySelector('.modal-icon-wrap').className = `modal-icon-wrap ${type}`;
  overlay.querySelector('.modal-icon-wrap i').className = `bi ${icon}`;
  overlay.querySelector('.modal-title').textContent = title;
  overlay.querySelector('.modal-desc').textContent = desc;
  overlay.querySelector('#confirmBtn').textContent = confirmLabel;
  overlay.querySelector('#confirmBtn').className = `btn-erp btn-erp-${type === 'danger' ? 'danger' : type === 'success' ? 'success' : 'primary'}`;
  overlay.querySelector('#cancelConfirmBtn').textContent = cancelLabel;
  overlay.classList.add('show');
  State.pendingAction = onConfirm;
}

function hideConfirm() {
  document.getElementById('confirmModal').classList.remove('show');
  State.pendingAction = null;
}

/* ============================================================
   STEP NAVIGATION
   ============================================================ */

function goToStep(stepNum) {
  $$('.workflow-step').forEach((s, i) => {
    s.classList.remove('active', 'completed');
    if (i + 1 < stepNum) s.classList.add('completed');
    else if (i + 1 === stepNum) s.classList.add('active');
  });

  $$('.step-section').forEach((sec, i) => {
    sec.style.display = (i + 1 === stepNum) ? 'block' : 'none';
  });
}

/* ============================================================
   CUSTOMER SEARCH & SELECTION
   ============================================================ */

function initCustomerSearch() {
  const searchInput = document.getElementById('customerSearch');
  const dropdown    = document.getElementById('customerDropdown');
  const hiddenInput = document.getElementById('hiddenCustomerId');

  if (!searchInput) return;

  // Build customer list from data
  searchInput.addEventListener('focus', () => {
    renderCustomerOptions(State.customers);
    dropdown.classList.add('show');
  });

  searchInput.addEventListener('input', () => {
    const q = searchInput.value.toLowerCase().trim();
    const filtered = State.customers.filter(c =>
      c.name.toLowerCase().includes(q) ||
      (c.mobile && c.mobile.includes(q)) ||
      (c.code && c.code.toLowerCase().includes(q))
    );
    renderCustomerOptions(filtered);
    dropdown.classList.add('show');
  });

  document.addEventListener('click', e => {
    if (!e.target.closest('.customer-search-container')) {
      dropdown.classList.remove('show');
    }
  });
}

function renderCustomerOptions(customers) {
  const dropdown = document.getElementById('customerDropdown');
  dropdown.innerHTML = '';
  if (!customers.length) {
    dropdown.innerHTML = '<div class="customer-option" style="pointer-events:none;color:var(--text-muted)"><i class="bi bi-search me-2"></i>No customers found</div>';
    return;
  }
  customers.forEach(c => {
    const initials = (c.name || 'C').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
    const div = document.createElement('div');
    div.className = 'customer-option';
    div.dataset.id = c.id;
    div.innerHTML = `
      <div class="customer-avatar">${initials}</div>
      <div>
        <div class="customer-option-name">${c.name}</div>
        <div class="customer-option-meta">${c.mobile || ''} ${c.code ? '· ' + c.code : ''}</div>
      </div>`;
    div.addEventListener('click', () => selectCustomer(c));
    dropdown.appendChild(div);
  });
}

function selectCustomer(c) {
  State.selectedCustomer = c;
  document.getElementById('customerSearch').value = c.name;
  document.getElementById('hiddenCustomerId').value = c.id;
  document.getElementById('customerDropdown').classList.remove('show');
  renderCustomerCard(c);
  updateWorkflowStep(1, true);
}

function renderCustomerCard(c) {
  const card = document.getElementById('customerInfoCard');
  if (!card) return;

  const dueAmount  = parseFloat(c.dueAmount || 0);
  const creditLimit = parseFloat(c.creditLimit || 10000);
  const duePct = Math.min(100, Math.round((dueAmount / creditLimit) * 100));

  card.innerHTML = `
    <div class="customer-info-header">
      <div>
        <div class="customer-name-display">${c.name}</div>
        <div class="mt-1">
          <span class="customer-type-tag ${(c.customerType || 'retail').toLowerCase()}">${c.customerType || 'Retail'}</span>
        </div>
      </div>
      <span class="customer-code-badge">${c.code || 'N/A'}</span>
    </div>
    <div class="customer-info-grid">
      <div class="customer-info-item">
        <label>Mobile</label>
        <div class="value"><i class="bi bi-telephone me-1"></i>${c.mobile || '—'}</div>
      </div>
      <div class="customer-info-item">
        <label>Email</label>
        <div class="value">${c.email || '—'}</div>
      </div>
      <div class="customer-info-item">
        <label>Address</label>
        <div class="value">${c.address || '—'}</div>
      </div>
      <div class="customer-info-item">
        <label>Business</label>
        <div class="value">${c.businessName || '—'}</div>
      </div>
    </div>
    <div class="customer-due-bar">
      <div class="customer-due-label">
        <span style="font-weight:600;font-size:12px;">Outstanding Balance</span>
        <span class="due-amount-value">${fmtMoney(dueAmount)}</span>
      </div>
      <div class="due-progress">
        <div class="due-progress-bar" style="width:${duePct}%"></div>
      </div>
      <div style="font-size:10px;color:var(--text-muted);margin-top:4px;">
        ${duePct}% of credit limit (${fmtMoney(creditLimit)})
      </div>
    </div>`;

  card.classList.add('visible');
}

function updateWorkflowStep(step, completed) {
  const stepEl = document.querySelector(`.workflow-step[data-step="${step}"]`);
  if (stepEl && completed) {
    stepEl.classList.add('completed');
    stepEl.classList.remove('active');
    const nextStep = document.querySelector(`.workflow-step[data-step="${step + 1}"]`);
    if (nextStep) nextStep.classList.add('active');
  }
}

/* ============================================================
   PRODUCT ROW MANAGEMENT
   ============================================================ */

function addProductRow() {
  const tbody = document.getElementById('itemsTableBody');
  const emptyState = document.getElementById('tableEmptyState');
  if (emptyState) emptyState.style.display = 'none';

  const idx = State.itemRowCount++;
  const row = document.createElement('tr');
  row.className = 'item-row';
  row.dataset.idx = idx;

  // Build product options
  let productOptions = '<option value="">— Select Product —</option>';
  State.products.forEach(p => {
    productOptions += `<option value="${p.id}" data-price="${p.price || 0}" data-stock="${p.stock || 0}">${p.name}</option>`;
  });

  row.innerHTML = `
    <td><span class="row-num">${idx + 1}</span></td>
    <td style="min-width:180px">
      <select class="item-product-select" name="items[${idx}].productId" onchange="onProductChange(this, ${idx})">
        ${productOptions}
      </select>
      <input type="hidden" name="items[${idx}].productId" class="product-id-hidden" value="">
    </td>
    <td>
      <span class="stock-badge out-stock" id="stock-${idx}">
        <i class="bi bi-box"></i> —
      </span>
    </td>
    <td>
      <input type="number" class="item-input qty-input" name="items[${idx}].quantity"
             id="qty-${idx}" value="1" min="1" placeholder="1"
             onchange="calcRowTotal(${idx})" oninput="calcRowTotal(${idx})">
    </td>
    <td>
      <input type="number" class="item-input price-input" name="items[${idx}].unitPrice"
             id="price-${idx}" value="0.00" min="0" step="0.01" placeholder="0.00"
             onchange="calcRowTotal(${idx})" oninput="calcRowTotal(${idx})">
    </td>
    <td>
      <input type="number" class="item-input disc-amount-input" name="items[${idx}].discountAmount"
             id="discAmt-${idx}" value="0.00" min="0" step="0.01" placeholder="0.00"
             onchange="onDiscAmountChange(${idx})" oninput="onDiscAmountChange(${idx})">
    </td>
    <td>
      <input type="number" class="item-input disc-pct-input" name="items[${idx}].discountPercentage"
             id="discPct-${idx}" value="0.00" min="0" max="100" step="0.01" placeholder="0%"
             onchange="onDiscPctChange(${idx})" oninput="onDiscPctChange(${idx})">
    </td>
    <td>
      <span class="line-total-cell" id="lineTotal-${idx}">৳ 0.00</span>
    </td>
    <td>
      <button type="button" class="btn-remove-row" onclick="removeProductRow(this)" title="Remove">
        <i class="bi bi-trash3"></i>
      </button>
    </td>`;

  tbody.appendChild(row);
  updateWorkflowStep(2, false);
  recalcSummary();
}

function onProductChange(selectEl, idx) {
  const opt = selectEl.options[selectEl.selectedIndex];
  const price = parseFloat(opt.dataset.price || 0);
  const stock = parseFloat(opt.dataset.stock || 0);
  const productId = opt.value;

  // Sync hidden input
  const hiddenPid = selectEl.closest('tr').querySelector('.product-id-hidden');
  if (hiddenPid) hiddenPid.value = productId;

  // Set price
  const priceInput = document.getElementById(`price-${idx}`);
  if (priceInput) priceInput.value = price.toFixed(2);

  // Update stock badge
  const stockBadge = document.getElementById(`stock-${idx}`);
  if (stockBadge) {
    if (!productId) {
      stockBadge.className = 'stock-badge out-stock';
      stockBadge.innerHTML = '<i class="bi bi-box"></i> —';
    } else if (stock <= 0) {
      stockBadge.className = 'stock-badge out-stock';
      stockBadge.innerHTML = `<i class="bi bi-x-circle"></i> Out`;
    } else if (stock < 5) {
      stockBadge.className = 'stock-badge low-stock';
      stockBadge.innerHTML = `<i class="bi bi-exclamation-triangle"></i> ${stock}`;
    } else {
      stockBadge.className = 'stock-badge in-stock';
      stockBadge.innerHTML = `<i class="bi bi-check-circle"></i> ${stock}`;
    }
  }

  calcRowTotal(idx);
}

function calcRowTotal(idx) {
  const qty       = numVal(document.getElementById(`qty-${idx}`));
  const price     = numVal(document.getElementById(`price-${idx}`));
  const discAmt   = numVal(document.getElementById(`discAmt-${idx}`));
  const subtotal  = qty * price;
  const lineTotal = Math.max(0, subtotal - discAmt);

  const lineTotalEl = document.getElementById(`lineTotal-${idx}`);
  if (lineTotalEl) lineTotalEl.textContent = fmtMoney(lineTotal);

  recalcSummary();
}

function onDiscAmountChange(idx) {
  const qty     = numVal(document.getElementById(`qty-${idx}`));
  const price   = numVal(document.getElementById(`price-${idx}`));
  const discAmt = numVal(document.getElementById(`discAmt-${idx}`));
  const subtotal = qty * price;
  if (subtotal > 0) {
    const pct = (discAmt / subtotal) * 100;
    const discPctEl = document.getElementById(`discPct-${idx}`);
    if (discPctEl) discPctEl.value = pct.toFixed(2);
  }
  calcRowTotal(idx);
}

function onDiscPctChange(idx) {
  const qty     = numVal(document.getElementById(`qty-${idx}`));
  const price   = numVal(document.getElementById(`price-${idx}`));
  const pct     = numVal(document.getElementById(`discPct-${idx}`));
  const subtotal = qty * price;
  const discAmt  = (subtotal * pct) / 100;
  const discAmtEl = document.getElementById(`discAmt-${idx}`);
  if (discAmtEl) discAmtEl.value = discAmt.toFixed(2);
  calcRowTotal(idx);
}

function removeProductRow(btn) {
  const row = btn.closest('tr');
  row.remove();
  renumberRows();
  recalcSummary();
  const tbody = document.getElementById('itemsTableBody');
  if (tbody && tbody.querySelectorAll('.item-row').length === 0) {
    const emptyState = document.getElementById('tableEmptyState');
    if (emptyState) emptyState.style.display = '';
  }
}

function renumberRows() {
  $$('.item-row').forEach((row, i) => {
    const numEl = row.querySelector('.row-num');
    if (numEl) numEl.textContent = i + 1;
  });
}

/* ============================================================
   SUMMARY CALCULATION
   ============================================================ */

function recalcSummary() {
  let subtotal   = 0;
  let totalDisc  = 0;

  $$('.item-row').forEach(row => {
    const idx = row.dataset.idx;
    const qty     = numVal(document.getElementById(`qty-${idx}`));
    const price   = numVal(document.getElementById(`price-${idx}`));
    const discAmt = numVal(document.getElementById(`discAmt-${idx}`));
    subtotal  += qty * price;
    totalDisc += discAmt;
  });

  const invoiceDisc = numVal(document.getElementById('invoiceDiscount'));
  const grandTotal  = Math.max(0, subtotal - totalDisc - invoiceDisc);
  const advancePaid = numVal(document.getElementById('advancePaidInput'));
  const dueAmount   = Math.max(0, grandTotal - advancePaid);

  setText('summarySubtotal',    fmtMoney(subtotal));
  setText('summaryItemDisc',    '- ' + fmtMoney(totalDisc));
  setText('summaryInvDisc',     '- ' + fmtMoney(invoiceDisc));
  setText('summaryGrandTotal',  fmtMoney(grandTotal));
  setText('summaryAdvancePaid', fmtMoney(advancePaid));
  setText('summaryDue',         fmtMoney(dueAmount));

  // Mobile sticky totals
  setText('mobileGrandTotal', fmtMoney(grandTotal));
  setText('mobileDueAmount',  fmtMoney(dueAmount));

  // Update due color
  const dueEl = document.getElementById('summaryDue');
  if (dueEl) dueEl.style.color = dueAmount > 0 ? 'var(--erp-danger)' : 'var(--erp-success)';
}

function setText(id, text) {
  const el = document.getElementById(id);
  if (el) el.textContent = text;
}

/* ============================================================
   PAYMENT METHOD SELECTION
   ============================================================ */

function initPaymentMethods() {
  $$('.payment-method-card').forEach(card => {
    card.addEventListener('click', () => {
      $$('.payment-method-card').forEach(c => c.classList.remove('selected'));
      card.classList.add('selected');
    });
  });
}

/* ============================================================
   FORM VALIDATION
   ============================================================ */

function validateForm() {
  const errors = [];

  // 1. Customer
  const custId = document.getElementById('hiddenCustomerId')?.value;
  if (!custId) {
    errors.push('Please select a customer.');
    document.getElementById('customerSearch')?.classList.add('is-invalid');
  } else {
    document.getElementById('customerSearch')?.classList.remove('is-invalid');
  }

  // 2. Sale Date
  const saleDate = document.getElementById('saleDateInput')?.value;
  if (!saleDate) {
    errors.push('Sale date is required.');
    document.getElementById('saleDateInput')?.classList.add('is-invalid');
  }

  // 3. At least one item
  const rows = $$('.item-row');
  if (rows.length === 0) {
    errors.push('Please add at least one product.');
  }

  // 4. Each item must have product and qty > 0
  rows.forEach((row, i) => {
    const idx = row.dataset.idx;
    const productSel = row.querySelector('.item-product-select');
    const qtyInput   = document.getElementById(`qty-${idx}`);
    if (!productSel?.value) {
      errors.push(`Row ${i + 1}: Please select a product.`);
      productSel?.classList.add('is-invalid');
    } else {
      productSel?.classList.remove('is-invalid');
    }
    if (numVal(qtyInput) <= 0) {
      errors.push(`Row ${i + 1}: Quantity must be greater than 0.`);
      qtyInput?.classList.add('is-invalid');
    } else {
      qtyInput?.classList.remove('is-invalid');
    }
  });

  return errors;
}

/* ============================================================
   BUTTON ACTIONS
   ============================================================ */

function handleSaveDraft(e) {
  e.preventDefault();
  const form = document.getElementById('salesForm');
  const errors = validateForm();
  if (errors.length) {
    showToast('error', 'Validation Error', errors[0]);
    return;
  }
  const btn = document.getElementById('btnSaveDraft');
  setButtonLoading(btn, true);

  document.getElementById('statusInput').value = 'DRAFT';
  setTimeout(() => {
    form.submit();
  }, 300);
}

function handleCompleteSale(e) {
  e.preventDefault();
  const errors = validateForm();
  if (errors.length) {
    showToast('error', 'Cannot Complete Sale', errors[0]);
    return;
  }

  showConfirm({
    type: 'success',
    icon: 'bi-check-circle',
    title: 'Complete Sale?',
    desc: 'This will finalize the invoice and update stock levels. This action cannot be undone.',
    confirmLabel: 'Yes, Complete Sale',
    onConfirm: () => {
      const form = document.getElementById('salesForm');
      const btn  = document.getElementById('btnComplete');
      setButtonLoading(btn, true);
      document.getElementById('statusInput').value = 'COMPLETED';
      hideConfirm();
      setTimeout(() => form.submit(), 200);
    },
  });
}

function handleCancel(e) {
  e.preventDefault();
  showConfirm({
    type: 'danger',
    icon: 'bi-x-circle',
    title: 'Discard Invoice?',
    desc: 'All entered data will be lost. Are you sure you want to cancel?',
    confirmLabel: 'Yes, Discard',
    onConfirm: () => {
      window.location.href = document.getElementById('cancelUrl')?.value || '/sales';
    },
  });
}

function setButtonLoading(btn, loading) {
  if (!btn) return;
  if (loading) btn.classList.add('loading');
  else btn.classList.remove('loading');
}

/* ============================================================
   INVOICE DISCOUNT
   ============================================================ */

function initInvoiceDiscountInput() {
  const el = document.getElementById('invoiceDiscount');
  if (el) el.addEventListener('input', recalcSummary);
}

function initAdvancePaidInput() {
  const el = document.getElementById('advancePaidInput');
  if (el) el.addEventListener('input', recalcSummary);
}

/* ============================================================
   LOAD DATA FROM DOM
   ============================================================ */

function loadDataFromDOM() {
    // No JSON parsing needed — data comes directly from Thymeleaf inline JS
    if (typeof PRODUCT_DATA  !== 'undefined') State.products  = PRODUCT_DATA;
    if (typeof CUSTOMER_DATA !== 'undefined') State.customers = CUSTOMER_DATA;
}

/* ============================================================
   STEP CLICK NAVIGATION (STEPPER HEADER)
   ============================================================ */

function initStepperNav() {
  $$('.workflow-step').forEach(step => {
    step.addEventListener('click', () => {
      const num = parseInt(step.dataset.step, 10);
      $$('.workflow-step').forEach((s, i) => {
        s.classList.remove('active');
        if (i + 1 === num) s.classList.add('active');
      });
    });
  });
}

/* ============================================================
   TODAY DATE DEFAULT
   ============================================================ */

function setDefaultDate() {
  const dateEl = document.getElementById('saleDateInput');
  if (dateEl && !dateEl.value) {
    const today = new Date().toISOString().slice(0, 10);
    dateEl.value = today;
  }
}

/* ============================================================
   INIT
   ============================================================ */

document.addEventListener('DOMContentLoaded', () => {
  loadDataFromDOM();
  initCustomerSearch();
  initPaymentMethods();
  initInvoiceDiscountInput();
  initAdvancePaidInput();
  initStepperNav();
  setDefaultDate();

  // Mark step 1 active
  const firstStep = document.querySelector('.workflow-step[data-step="1"]');
  if (firstStep) firstStep.classList.add('active');

  // Confirm modal buttons
  document.getElementById('confirmBtn')?.addEventListener('click', () => {
    if (State.pendingAction) State.pendingAction();
  });

  document.getElementById('cancelConfirmBtn')?.addEventListener('click', hideConfirm);

  document.getElementById('confirmModal')?.addEventListener('click', function (e) {
    if (e.target === this) hideConfirm();
  });

  // Wire action buttons
  document.getElementById('btnSaveDraft')?.addEventListener('click', handleSaveDraft);
  document.getElementById('btnComplete')?.addEventListener('click', handleCompleteSale);
  document.getElementById('btnCancel')?.addEventListener('click', handleCancel);
  document.getElementById('btnBack')?.addEventListener('click', () => window.history.back());

  // Add first product row automatically
  // (Only if form has no pre-populated items)
  const existingRows = $$('.item-row');
  if (existingRows.length === 0) {
    // leave empty - user clicks button
  }

  recalcSummary();
});