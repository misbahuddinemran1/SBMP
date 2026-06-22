/* ============================================================
   eLoan SaaS — Purchase Module JS
   Vanilla JS, no jQuery
   ============================================================ */

'use strict';

/* ── State ─────────────────────────────────────── */
const PUR = {
  rowIndex: 0,
  suppliers: window.__PUR_SUPPLIERS || [],
  categories: window.__PUR_CATEGORIES || [],
  products: window.__PUR_PRODUCTS || [],
};

/* ── DOM Ready ─────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  initTooltips();
  initSearchBar();
  initPurchaseForm();
  initTableSearch();
  initAlertDismiss();
});

/* ── Tooltips ──────────────────────────────────── */
function initTooltips() {
  if (typeof bootstrap === 'undefined') return;
  document.querySelectorAll('[data-bs-toggle="tooltip"]')
    .forEach(el => new bootstrap.Tooltip(el, { trigger: 'hover' }));
}

/* ── Alert Auto-dismiss ────────────────────────── */
function initAlertDismiss() {
  const alert = document.querySelector('.pur-alert-success');
  if (alert) {
    setTimeout(() => {
      alert.style.transition = 'opacity .5s ease';
      alert.style.opacity = '0';
      setTimeout(() => alert.remove(), 500);
    }, 3500);
  }
}

/* ── List page search (client-side) ───────────── */
function initTableSearch() {
  const inp = document.getElementById('purchaseSearch');
  if (!inp) return;
  inp.addEventListener('input', () => {
    const q = inp.value.toLowerCase().trim();
    document.querySelectorAll('.pur-tr-searchable').forEach(tr => {
      tr.style.display = tr.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
    const visible = document.querySelectorAll('.pur-tr-searchable:not([style*="none"])').length;
    const noResult = document.getElementById('noSearchResult');
    if (noResult) noResult.style.display = visible === 0 ? '' : 'none';
  });
}

/* ── Simple search bar focus highlight ─────────── */
function initSearchBar() {
  document.querySelectorAll('.pur-search-bar input').forEach(inp => {
    inp.addEventListener('focus', () => inp.parentElement.classList.add('focused'));
    inp.addEventListener('blur', () => inp.parentElement.classList.remove('focused'));
  });
}

/* ════════════════════════════════════════════════
   PURCHASE FORM
   ════════════════════════════════════════════════ */
function initPurchaseForm() {
  if (!document.getElementById('purchaseForm')) return;

  buildSupplierSelector();
  attachBillingListeners();
  addProductRow(); // start with one row
  recalcAll();

  // Save with loading
  const form = document.getElementById('purchaseForm');
  form.addEventListener('submit', e => handleFormSubmit(e, 'save'));

  const printBtn = document.getElementById('savePrintBtn');
  if (printBtn) {
    printBtn.addEventListener('click', () => {
      const hidden = document.getElementById('printAfterSave');
      if (hidden) hidden.value = 'true';
      form.requestSubmit ? form.requestSubmit() : form.submit();
    });
  }
}

/* ── Supplier Selector Logic ───────────────────── */
function buildSupplierSelector() {
  const supplierSel = document.getElementById('supplierId');
  if (!supplierSel) return;

  // Attach change — show/hide supplier info banner
  supplierSel.addEventListener('change', () => {
    const sid = supplierSel.value;
    updateSupplierBanner(sid);
  });
}

function updateSupplierBanner(supplierId) {
  const banner = document.getElementById('supplierInfoBanner');
  const nameEl = document.getElementById('bannerSupplierName');
  const companyEl = document.getElementById('bannerSupplierCompany');
  if (!banner) return;

  if (!supplierId) {
    banner.classList.remove('visible');
    return;
  }

  // Use whatever info we have in the select option
  const opt = document.querySelector(`#supplierId option[value="${supplierId}"]`);
  if (opt) {
    if (nameEl) nameEl.textContent = opt.textContent.trim();
    if (companyEl) companyEl.textContent = opt.dataset.company || '';
  }
  banner.classList.add('visible');
}

/* ════════════════════════════════════════════════
   PRODUCT ROWS
   ════════════════════════════════════════════════ */
function addProductRow() {
  const tbody = document.getElementById('productRowsBody');
  if (!tbody) return;

  const idx = PUR.rowIndex++;
  const row = document.createElement('tr');
  row.className = 'product-row';
  row.dataset.idx = idx;

  row.innerHTML = `
    <td>
      <select name="items[${idx}].categoryId" 
              class="pur-control pur-control-sm cat-select" 
              onchange="onCategoryChange(this, ${idx})" 
              required>
        <option value="">Select Category</option>
        ${buildCategoryOptions()}
      </select>
    </td>
    <td>
      <select name="items[${idx}].productId" 
              class="pur-control pur-control-sm prod-select" 
              id="prodSel_${idx}" 
              onchange="onProductChange(this, ${idx})"
              required disabled>
        <option value="">Select Category First</option>
      </select>
    </td>
    <td>
      <span class="pur-badge stock-badge-ok" id="stockBadge_${idx}" style="display:none"></span>
    </td>
    <td>
      <input type="number" name="items[${idx}].quantity" 
             class="pur-control pur-control-sm qty-input" 
             id="qty_${idx}"
             placeholder="0" min="1" value="1"
             oninput="recalcRow(${idx})" required style="width:80px">
    </td>
    <td>
      <input type="number" name="items[${idx}].unitPrice" 
             class="pur-control pur-control-sm price-input" 
             id="price_${idx}"
             placeholder="0.00" min="0" step="0.01" value="0"
             oninput="recalcRow(${idx})" required style="width:100px">
    </td>
    <td>
      <input type="number" name="items[${idx}].discount" 
             class="pur-control pur-control-sm disc-input" 
             id="disc_${idx}"
             placeholder="0.00" min="0" step="0.01" value="0"
             oninput="recalcRow(${idx})" style="width:90px">
    </td>
    <td>
      <input type="hidden" name="items[${idx}].subtotal" id="sub_${idx}" value="0">
      <span class="row-total-cell" id="rowTotal_${idx}">৳0.00</span>
    </td>
    <td>
      <button type="button" class="btn-pur-danger" onclick="removeProductRow(this, ${idx})" 
              title="Remove row">
        <i class="bi bi-trash3"></i>
      </button>
    </td>
  `;

  tbody.appendChild(row);
  recalcRow(idx);

  // auto-focus qty after adding row
  setTimeout(() => {
    const catSel = row.querySelector('.cat-select');
    if (catSel) catSel.focus();
  }, 50);
}

function buildCategoryOptions() {
  return (PUR.categories || [])
    .map(c => `<option value="${c.id}">${escHtml(c.name)}</option>`)
    .join('');
}

function onCategoryChange(catSel, idx) {
  const catId = catSel.value;
  const prodSel = document.getElementById(`prodSel_${idx}`);
  if (!prodSel) return;

  prodSel.innerHTML = '<option value="">Loading...</option>';
  prodSel.disabled = true;

  if (!catId) {
    prodSel.innerHTML = '<option value="">Select Category First</option>';
    return;
  }

  // Filter products by category
  const filtered = (PUR.products || []).filter(p => String(p.categoryId) === String(catId));
  if (filtered.length === 0) {
    prodSel.innerHTML = '<option value="">No Products Found</option>';
    return;
  }

  prodSel.innerHTML = '<option value="">Select Product</option>' +
    filtered.map(p => {
      let stockInfo = '';
      if (p.stock !== undefined) stockInfo = ` (Stock: ${p.stock})`;
      return `<option value="${p.id}" data-price="${p.price || 0}" data-stock="${p.stock || 0}" data-unit="${escHtml(p.unit || '')}">${escHtml(p.name)}${stockInfo}</option>`;
    }).join('');

  prodSel.disabled = false;
}

function onProductChange(prodSel, idx) {
  const opt = prodSel.options[prodSel.selectedIndex];
  if (!opt || !opt.value) return;

  const price = parseFloat(opt.dataset.price) || 0;
  const stock = parseInt(opt.dataset.stock) || 0;

  const priceInput = document.getElementById(`price_${idx}`);
  if (priceInput) priceInput.value = price.toFixed(2);

  // Stock badge
  const badge = document.getElementById(`stockBadge_${idx}`);
  if (badge) {
    badge.style.display = '';
    if (stock === 0) {
      badge.textContent = 'Out of Stock';
      badge.className = 'pur-badge stock-badge-critical';
    } else if (stock < 10) {
      badge.textContent = `Low: ${stock}`;
      badge.className = 'pur-badge stock-badge-low';
    } else {
      badge.textContent = `In Stock: ${stock}`;
      badge.className = 'pur-badge stock-badge-ok';
    }
  }

  // auto-focus qty
  const qtyEl = document.getElementById(`qty_${idx}`);
  if (qtyEl) { qtyEl.select(); }

  recalcRow(idx);
}

function removeProductRow(btn, idx) {
  const row = btn.closest('.product-row');
  if (!row) return;
  // Don't remove last row
  const allRows = document.querySelectorAll('.product-row');
  if (allRows.length <= 1) {
    row.style.animation = 'none';
    row.style.background = 'rgba(239,68,68,.08)';
    setTimeout(() => row.style.background = '', 600);
    return;
  }
  row.style.opacity = '0';
  row.style.transform = 'translateX(20px)';
  row.style.transition = 'all .2s ease';
  setTimeout(() => { row.remove(); recalcAll(); }, 200);
}

/* ── Calculations ──────────────────────────────── */
function recalcRow(idx) {
  const qty   = parseFloat(document.getElementById(`qty_${idx}`)?.value)   || 0;
  const price = parseFloat(document.getElementById(`price_${idx}`)?.value) || 0;
  const disc  = parseFloat(document.getElementById(`disc_${idx}`)?.value)  || 0;
  const total = Math.max(0, (qty * price) - disc);

  const subHidden = document.getElementById(`sub_${idx}`);
  const totalSpan = document.getElementById(`rowTotal_${idx}`);
  if (subHidden) subHidden.value = total.toFixed(2);
  if (totalSpan) totalSpan.textContent = '৳' + formatNum(total);

  recalcSummary();
}

function recalcAll() {
  document.querySelectorAll('.product-row').forEach(row => {
    const idx = row.dataset.idx;
    recalcRow(idx);
  });
}

function recalcSummary() {
  let subtotal = 0;
  document.querySelectorAll('[id^="sub_"]').forEach(el => {
    subtotal += parseFloat(el.value) || 0;
  });

  const discEl    = document.getElementById('invoiceDiscount');
  const advEl     = document.getElementById('advancePaidInput');
  const discount  = parseFloat(discEl?.value) || 0;
  const advance   = parseFloat(advEl?.value)  || 0;
  const final     = Math.max(0, subtotal - discount);
  const due       = Math.max(0, final - advance);

  // Update hidden fields
  setVal('subtotalHidden', subtotal.toFixed(2));
  setVal('finalAmountHidden', final.toFixed(2));
  setVal('dueAmountHidden', due.toFixed(2));

  // Update display
  setText('displaySubtotal', '৳' + formatNum(subtotal));
  setText('displayDiscount', '৳' + formatNum(discount));
  setText('displayFinal', '৳' + formatNum(final));
  setText('displayAdvance', '৳' + formatNum(advance));
  setText('displayDue', '৳' + formatNum(due));
}

function attachBillingListeners() {
  ['invoiceDiscount', 'advancePaidInput'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.addEventListener('input', recalcSummary);
  });
}

/* ── Form Submit ───────────────────────────────── */
function handleFormSubmit(e, type) {
  const form = e.target;
  if (!form.checkValidity()) {
    e.preventDefault();
    form.classList.add('was-validated');
    showToast('Please fill all required fields.', 'warning');
    return;
  }

  const btn = document.getElementById('savePurchaseBtn');
  if (btn) {
    btn.classList.add('btn-loading');
    btn.innerHTML = '<span class="spinner-xs"></span> Saving...';
  }
}

/* ── Utilities ─────────────────────────────────── */
function formatNum(n) {
  return Number(n).toLocaleString('en-BD', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function setVal(id, val) {
  const el = document.getElementById(id);
  if (el) el.value = val;
}

function setText(id, text) {
  const el = document.getElementById(id);
  if (el) el.textContent = text;
}

function escHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function showToast(msg, type = 'info') {
  const colors = { info: '#2563eb', warning: '#f59e0b', success: '#10b981', danger: '#ef4444' };
  const t = document.createElement('div');
  t.style.cssText = `
    position:fixed; bottom:1.5rem; right:1.5rem; z-index:9999;
    background:${colors[type] || colors.info}; color:white;
    padding:.75rem 1.25rem; border-radius:10px;
    font-size:.875rem; font-weight:600;
    box-shadow:0 8px 24px rgba(0,0,0,.2);
    animation:slideUp .25s ease;
    font-family:'Plus Jakarta Sans',sans-serif;
  `;
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => { t.style.opacity = '0'; t.style.transition = 'opacity .3s'; setTimeout(() => t.remove(), 300); }, 3000);
}

/* ── Print memo ────────────────────────────────── */
function printMemo() {
  window.print();
}

/* ── Expose globals needed by inline HTML ─────── */
window.addProductRow = addProductRow;
window.removeProductRow = removeProductRow;
window.onCategoryChange = onCategoryChange;
window.onProductChange = onProductChange;
window.recalcRow = recalcRow;
window.printMemo = printMemo;
