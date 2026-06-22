/* ============================================================
   purchase-new.js  —  New Purchase Invoice Logic
   NOTE: SUPPLIERS_DATA, CATEGORIES_DATA, PRODUCTS_DATA are
         injected by Thymeleaf in the HTML file before this
         script is loaded.
   ============================================================ */

// ============================================================
//  STATE
// ============================================================
let selectedCategories = [];
let invDiscType  = 'amt';
let itemRowCount = 0;
let payRowCount  = 0;

// ============================================================
//  INIT
// ============================================================
document.addEventListener('DOMContentLoaded', function () {

    // Set today's date
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('purchaseDateInput').value = today;

    // Generate draft invoice number (will be replaced by server on save)
    document.getElementById('invoiceNo').textContent =
        'INV-' + new Date().getFullYear() + '-' + String(Math.floor(Math.random() * 9000) + 1000);

    // Populate category dropdown from injected data
    renderCategoryOptions();
    renderCatParentOptions();

    // Sync visible status select → hidden status field
    const statusDisplay = document.getElementById('invoiceStatusDisplay');
    if (statusDisplay) {
        statusDisplay.addEventListener('change', function () {
            document.getElementById('statusField').value = this.value;
            updateStatusChip(this.value);
        });
    }

    // Close category dropdown when clicking outside
    document.addEventListener('click', function (e) {
        const selector = document.getElementById('categorySelector');
        const dropdown = document.getElementById('catDropdown');
        if (selector && dropdown &&
            !selector.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.remove('open');
        }
    });
});

// ============================================================
//  SUPPLIER
// ============================================================
function onSupplierChange(select) {
    const opt = select.options[select.selectedIndex];
    const box = document.getElementById('supplierInfoBox');
    if (!select.value) {
        box.classList.remove('visible');
        return;
    }
    document.getElementById('supplierCompany').textContent = opt.dataset.company || '—';
    document.getElementById('supplierMeta').textContent =
        (opt.dataset.phone || '') + (opt.dataset.address ? ' · ' + opt.dataset.address : '');
    box.classList.add('visible');
}

// ============================================================
//  STATUS CHIP
// ============================================================
function updateStatusChip(status) {
    const chip = document.getElementById('draftStatusChip');
    chip.className = 'status-chip ' + status.toLowerCase();
    chip.innerHTML = '<span class="dot"></span> ' +
        (status === 'DRAFT' ? 'Draft' : status === 'COMPLETED' ? 'Completed' : 'Cancelled');
}

// ============================================================
//  CATEGORY MULTI-SELECT
// ============================================================
function renderCategoryOptions(filter = '') {
    const list = document.getElementById('catOptionsList');
    list.innerHTML = '';
    CATEGORIES_DATA
        .filter(c => c.name.toLowerCase().includes(filter.toLowerCase()))
        .forEach(cat => {
            const isSelected = selectedCategories.some(s => s.id === cat.id);
            const div = document.createElement('div');
            div.className = 'cat-dropdown-item' + (isSelected ? ' selected' : '');
            div.dataset.id = cat.id;
            div.innerHTML = `<span class="check-icon">✓</span>${cat.name}`;
            div.addEventListener('click', function (e) {
                e.stopPropagation();
                toggleCategory(cat);
            });
            list.appendChild(div);
        });
}

function toggleCatDropdown() {
    const dd = document.getElementById('catDropdown');
    dd.classList.toggle('open');
    if (dd.classList.contains('open')) {
        document.getElementById('catSearchInput').focus();
    }
}

function filterCategories(val) {
    renderCategoryOptions(val);
}

function toggleCategory(cat) {
    const idx = selectedCategories.findIndex(c => c.id === cat.id);
    if (idx > -1) {
        selectedCategories.splice(idx, 1);
    } else {
        selectedCategories.push(cat);
    }
    renderCategoryChips();
    renderCategoryOptions(document.getElementById('catSearchInput').value);
    updateProductSelectOptions();
}

function renderCategoryChips() {
    const selector = document.getElementById('categorySelector');
    const placeholder = document.getElementById('catPlaceholder');
    selector.querySelectorAll('.cat-chip').forEach(c => c.remove());

    if (selectedCategories.length === 0) {
        placeholder.style.display = '';
    } else {
        placeholder.style.display = 'none';
        selectedCategories.forEach(cat => {
            const chip = document.createElement('span');
            chip.className = 'cat-chip';
            chip.innerHTML = `${cat.name}
                <span class="remove-chip"
                      onclick="event.stopPropagation(); removeCategory(${cat.id})">×</span>`;
            selector.insertBefore(chip, placeholder);
        });
    }
    updateSummaryPills();
}

function removeCategory(id) {
    selectedCategories = selectedCategories.filter(c => c.id !== id);
    renderCategoryChips();
    renderCategoryOptions();
    updateProductSelectOptions();
}

function getFilteredProducts() {
    if (selectedCategories.length === 0) return PRODUCTS_DATA;
    const ids = selectedCategories.map(c => c.id);
    return PRODUCTS_DATA.filter(p => ids.includes(p.categoryId));
}

function populateProductSelect(select) {
    const currentVal = select.value;
    select.innerHTML = '<option value="">-- Select Product --</option>';
    getFilteredProducts().forEach(p => {
        const opt = document.createElement('option');
        opt.value       = p.id;
        opt.textContent = p.name + ' (' + (p.unit || 'PCS') + ')';
        opt.dataset.stock      = p.stock;
        opt.dataset.lastPrice  = p.lastPrice;
        opt.dataset.categoryId = p.categoryId;
        select.appendChild(opt);
    });
    if (currentVal) select.value = currentVal;
}

function updateProductSelectOptions() {
    document.querySelectorAll('.product-select').forEach(select => {
        populateProductSelect(select);
    });
}

// ============================================================
//  ITEMS TABLE
// ============================================================
function addItemRow() {
    const container = document.getElementById('itemRowsContainer');
    hideNoProductsRow();

    const idx  = itemRowCount++;
    const rowN = container.querySelectorAll('tr:not(#noProductsRow)').length + 1;

    const tr = document.createElement('tr');
    tr.id = 'itemRow-' + idx;
    tr.innerHTML = `
        <td><span class="row-num">${rowN}</span></td>

        <td>
            <select name="items[${idx}].productId"
                    class="table-input product-select" required
                    onchange="onProductChange(this)">
                <option value="">-- Select Product --</option>
            </select>
            <input type="hidden" name="items[${idx}].categoryId" class="category-hidden-id" />
        </td>

        <td style="text-align:center;">
            <span class="stock-badge" id="stockBadge-${idx}">—</span>
        </td>

        <td style="text-align:center;">
            <span class="last-price-badge" id="lastPriceBadge-${idx}">—</span>
        </td>

        <td>
            <input type="number" step="0.001" min="0.001"
                   name="items[${idx}].purchaseQty"
                   class="table-input qty-input" required value="1"
                   oninput="calculateRowTotal(this)"
                   onkeydown="handleEnterKey(event,this)" />
        </td>

        <td>
            <input type="number" step="0.01" min="0"
                   name="items[${idx}].unitPrice"
                   class="table-input price-input" required
                   placeholder="0.00"
                   oninput="calculateRowTotal(this)"
                   onkeydown="handleEnterKey(event,this)" />
        </td>

        <td>
            <div class="disc-type-toggle" style="margin-bottom:4px;">
                <button type="button" class="disc-type-btn active"
                        onclick="toggleRowDiscType(this,'amt')">৳</button>
                <button type="button" class="disc-type-btn"
                        onclick="toggleRowDiscType(this,'pct')">%</button>
            </div>
            <input type="number" step="0.01" min="0"
                   name="items[${idx}].discountAmount"
                   class="table-input disc-input"
                   placeholder="0.00" data-disc-type="amt"
                   oninput="calculateRowTotal(this)"
                   onkeydown="handleEnterKey(event,this)" />
            <input type="hidden" name="items[${idx}].discountPercentage"
                   class="disc-pct-hidden" value="0" />
        </td>

        <td>
            <div class="row-total-cell" id="rowTotal-${idx}">৳ 0.00</div>
        </td>

        <td style="text-align:center;">
            <button type="button" class="remove-row-btn"
                    onclick="removeItemRow(this)">✕</button>
        </td>`;

    container.appendChild(tr);
    populateProductSelect(tr.querySelector('.product-select'));
    reNumberRows();
    showToast('Product row added', 'info');
}

function onProductChange(select) {
    const row   = select.closest('tr');
    const rowId = row.id.split('-')[1];
    const opt   = select.options[select.selectedIndex];

    if (!select.value) {
        document.getElementById('stockBadge-'    + rowId).textContent = '—';
        document.getElementById('stockBadge-'    + rowId).className   = 'stock-badge';
        document.getElementById('lastPriceBadge-'+ rowId).textContent = '—';
        return;
    }

    // Duplicate product guard
    let dupCount = 0;
    document.querySelectorAll('.product-select').forEach(s => {
        if (s.value === select.value && s !== select) dupCount++;
    });
    if (dupCount > 0) {
        showToast('⚠️ This product is already in the list!', 'warning');
        select.value = '';
        return;
    }

    const stock     = parseFloat(opt.dataset.stock)     || 0;
    const lastPrice = parseFloat(opt.dataset.lastPrice)  || 0;
    const catId     = opt.dataset.categoryId;

    // Stock badge
    const badge = document.getElementById('stockBadge-' + rowId);
    if      (stock <= 0)  { badge.className = 'stock-badge out-stock'; badge.textContent = '0 (Out)';        }
    else if (stock <= 10) { badge.className = 'stock-badge low-stock'; badge.textContent = stock + ' (Low)'; }
    else                  { badge.className = 'stock-badge in-stock';  badge.textContent = stock;            }

    // Last price badge
    document.getElementById('lastPriceBadge-' + rowId).textContent = '৳' + lastPrice.toFixed(2);

    // Auto-fill last purchase price
    const priceInput = row.querySelector('.price-input');
    if (!priceInput.value && lastPrice > 0) {
        priceInput.value = lastPrice.toFixed(2);
        showToast('Last price auto-filled: ৳' + lastPrice.toFixed(2), 'info');
    }

    // Set hidden category id
    row.querySelector('.category-hidden-id').value = catId;

    calculateRowTotal(row.querySelector('.qty-input'));
}

function toggleRowDiscType(btn, type) {
    const toggle = btn.closest('.disc-type-toggle');
    toggle.querySelectorAll('.disc-type-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    const row       = btn.closest('tr');
    const discInput = row.querySelector('.disc-input');
    discInput.dataset.discType = type;
    discInput.value            = '';

    // Swap the name attribute so correct field is submitted
    const idx = row.id.split('-')[1];
    if (type === 'pct') {
        discInput.name = `items[${idx}].discountPercentage`;
        row.querySelector('.disc-pct-hidden').name = `items[${idx}].discountAmount`;
        row.querySelector('.disc-pct-hidden').value = '0';
    } else {
        discInput.name = `items[${idx}].discountAmount`;
        row.querySelector('.disc-pct-hidden').name  = `items[${idx}].discountPercentage`;
        row.querySelector('.disc-pct-hidden').value = '0';
    }

    calculateRowTotal(discInput);
}

function calculateRowTotal(input) {
    const row = input.closest('tr');
    if (!row) return;
    const rowId = row.id.split('-')[1];

    const qty       = parseFloat(row.querySelector('.qty-input')  ?.value) || 0;
    const price     = parseFloat(row.querySelector('.price-input') ?.value) || 0;
    const discInput = row.querySelector('.disc-input');
    const discVal   = parseFloat(discInput?.value) || 0;
    const discType  = discInput?.dataset.discType || 'amt';
    const subtotal  = qty * price;

    const discAmt = (discType === 'pct')
        ? (subtotal * discVal) / 100
        : discVal;

    const rowTotal = Math.max(0, subtotal - discAmt);
    const totalEl  = document.getElementById('rowTotal-' + rowId);
    if (totalEl) totalEl.textContent = '৳ ' + rowTotal.toFixed(2);

    calculateGrandSummary();
}

function removeItemRow(btn) {
    const container = document.getElementById('itemRowsContainer');
    const realRows  = container.querySelectorAll('tr:not(#noProductsRow)');
    if (realRows.length <= 1) {
        showToast('At least one product is required', 'error');
        return;
    }
    btn.closest('tr').remove();
    reNumberRows();
    calculateGrandSummary();
}

function reNumberRows() {
    const rows = document.querySelectorAll('#itemRowsContainer tr:not(#noProductsRow)');
    rows.forEach((row, i) => {
        const numEl = row.querySelector('.row-num');
        if (numEl) numEl.textContent = i + 1;
    });
    document.getElementById('itemCountBadge').textContent =
        rows.length + ' product' + (rows.length !== 1 ? 's' : '');
    updateSummaryPills();
    if (rows.length === 0) showNoProductsRow();
    else                   hideNoProductsRow();
}

function showNoProductsRow() { document.getElementById('noProductsRow').style.display = ''; }
function hideNoProductsRow() { document.getElementById('noProductsRow').style.display = 'none'; }

// ============================================================
//  PAYMENT TABLE
// ============================================================
function addPaymentRow() {
    const container = document.getElementById('paymentRowsContainer');
    const idx       = payRowCount++;
    const today     = new Date().toISOString().split('T')[0];

    const tr = document.createElement('tr');
    tr.id = 'paymentRow-' + idx;

    /*
     * FIXED: PaymentMethod enum values match backend exactly:
     *   CASH | BANK | CHEQUE | BKASH | NAGAD | ROCKET | CARD
     */
    tr.innerHTML = `
        <td>
            <select name="payments[${idx}].paymentMethod"
                    class="table-input" required>
                <option value="CASH">💵 Cash</option>
                <option value="BANK">🏦 Bank Transfer</option>
                <option value="CHEQUE">📄 Cheque</option>
                <option value="BKASH">📱 Bkash</option>
                <option value="NAGAD">📱 Nagad</option>
                <option value="ROCKET">🚀 Rocket</option>
                <option value="CARD">💳 Card</option>
            </select>
        </td>
        <td>
            <input type="number" step="0.01" min="0.01"
                   name="payments[${idx}].amount"
                   class="table-input payment-amount-input" required
                   placeholder="0.00"
                   oninput="calculateGrandSummary()" />
        </td>
        <td>
            <input type="text"
                   name="payments[${idx}].referenceNo"
                   class="table-input"
                   placeholder="Txn ID / Cheque No" />
        </td>
        <td>
            <input type="text"
                   name="payments[${idx}].accountInfo"
                   class="table-input"
                   placeholder="Account / Mobile No" />
        </td>
        <td>
            <input type="date"
                   name="payments[${idx}].paymentDate"
                   class="table-input" value="${today}" />
        </td>
        <td style="text-align:center;">
            <button type="button" class="remove-row-btn"
                    onclick="removePaymentRow(this)">✕</button>
        </td>`;

    container.appendChild(tr);
    calculateGrandSummary();
}

function removePaymentRow(btn) {
    btn.closest('tr').remove();
    calculateGrandSummary();
}

// ============================================================
//  GRAND SUMMARY  (display-only — server recalculates on save)
// ============================================================
function setInvDiscType(type) {
    invDiscType = type;
    document.getElementById('discTypeAmt').classList.toggle('active', type === 'amt');
    document.getElementById('discTypePct').classList.toggle('active', type === 'pct');
    document.getElementById('discSymbol').textContent = type === 'pct' ? '%' : '৳';
    document.getElementById('invDiscInput').value = '';
    calculateGrandSummary();
}

function calculateGrandSummary() {
    let subtotal     = 0;
    let itemDiscount = 0;

    document.querySelectorAll('#itemRowsContainer tr:not(#noProductsRow)').forEach(row => {
        const qty       = parseFloat(row.querySelector('.qty-input')  ?.value) || 0;
        const price     = parseFloat(row.querySelector('.price-input') ?.value) || 0;
        const discInput = row.querySelector('.disc-input');
        const discVal   = parseFloat(discInput?.value) || 0;
        const discType  = discInput?.dataset.discType  || 'amt';
        const sub       = qty * price;
        subtotal += sub;
        itemDiscount += (discType === 'pct') ? (sub * discVal) / 100 : discVal;
    });

    /*
     * NOTE: Invoice-level discount is DISPLAY-ONLY.
     * Backend (PurchaseServiceImpl) does not process invoiceDiscount —
     * grandTotal = subtotal − itemDiscounts only.
     * This section helps the user preview; actual grand total is computed server-side.
     */
    const discInputVal = parseFloat(document.getElementById('invDiscInput').value) || 0;
    let invDisc = 0;
    if (invDiscType === 'pct') invDisc = ((subtotal - itemDiscount) * discInputVal) / 100;
    else                        invDisc = discInputVal;

    const grandTotal = Math.max(0, subtotal - itemDiscount - invDisc);

    let totalPaid = 0;
    document.querySelectorAll('.payment-amount-input').forEach(i => {
        totalPaid += parseFloat(i.value) || 0;
    });

    const netDue = Math.max(0, grandTotal - totalPaid);

    // Update voucher display
    document.getElementById('vSubtotal').textContent     = '৳ ' + subtotal.toFixed(2);
    document.getElementById('vItemDiscount').textContent = itemDiscount.toFixed(2);
    document.getElementById('vInvDiscount').textContent  = invDisc.toFixed(2);
    document.getElementById('vGrandTotal').textContent   = grandTotal.toFixed(2);
    document.getElementById('voucherGrand').textContent  = '৳ ' + grandTotal.toFixed(2);
    document.getElementById('vTotalPaid').textContent    = grandTotal > 0 ? totalPaid.toFixed(2) : '0.00';
    document.getElementById('vNetDueFull').textContent   = '৳ ' + netDue.toFixed(2);

    // Due color
    const dueEl = document.getElementById('vNetDueFull');
    dueEl.style.color = (netDue <= 0 && grandTotal > 0) ? 'var(--success)' : 'var(--warning)';
}

// ============================================================
//  SUMMARY PILLS
// ============================================================
function updateSummaryPills() {
    const pills    = document.getElementById('summaryPills');
    pills.innerHTML = '';
    const itemRows = document.querySelectorAll('#itemRowsContainer tr:not(#noProductsRow)').length;

    if (selectedCategories.length > 0) {
        const p = document.createElement('div');
        p.className   = 'summary-pill pill-cats';
        p.textContent = selectedCategories.length +
            ' categor' + (selectedCategories.length > 1 ? 'ies' : 'y');
        pills.appendChild(p);
    }
    if (itemRows > 0) {
        const p = document.createElement('div');
        p.className   = 'summary-pill pill-items';
        p.textContent = itemRows + ' product line' + (itemRows > 1 ? 's' : '');
        pills.appendChild(p);
    }
}

// ============================================================
//  SUBMIT ACTIONS
// ============================================================
function setStatus(status) {
    document.getElementById('statusField').value = status;
    document.getElementById('invoiceStatusDisplay').value = status;
    updateStatusChip(status);

    // Default blank discounts to 0 before submit
    document.querySelectorAll('.disc-input').forEach(input => {
        if (input.value === '') input.value = '0';
    });
    calculateGrandSummary();
}

// ============================================================
//  ADD CATEGORY MODAL
// ============================================================
function openAddCategory() {
    document.getElementById('catDropdown').classList.remove('open');
    renderCatParentOptions();
    openModal('addCategoryModal');
}

function renderCatParentOptions() {
    // Parent category select
    const parentSel = document.getElementById('newCatParent');
    parentSel.innerHTML = '<option value="">-- None (Top Level) --</option>';
    CATEGORIES_DATA.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id; opt.textContent = c.name;
        parentSel.appendChild(opt);
    });

    // New product category select
    const prodCatSel = document.getElementById('newProdCategory');
    prodCatSel.innerHTML = '<option value="">-- Select Category --</option>';
    CATEGORIES_DATA.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id; opt.textContent = c.name;
        prodCatSel.appendChild(opt);
    });
}

function submitNewCategory() {
    const name = document.getElementById('newCatName').value.trim();
    if (!name) { showToast('Category name is required', 'error'); return; }

    const newCat = {
        id:   Date.now(),
        name: name,
        code: document.getElementById('newCatCode').value,
        desc: document.getElementById('newCatDesc').value
    };
    CATEGORIES_DATA.push(newCat);
    selectedCategories.push(newCat);
    renderCategoryChips();
    renderCategoryOptions();
    updateProductSelectOptions();
    closeModal('addCategoryModal');
    showToast('Category "' + name + '" added!', 'success');

    // Reset fields
    ['newCatName', 'newCatCode', 'newCatDesc'].forEach(id => {
        document.getElementById(id).value = '';
    });
}

// ============================================================
//  ADD PRODUCT MODAL
// ============================================================
function submitNewProduct() {
    const name  = document.getElementById('newProdName').value.trim();
    const catId = parseInt(document.getElementById('newProdCategory').value);
    if (!name)  { showToast('Product name is required',    'error'); return; }
    if (!catId) { showToast('Please select a category', 'error'); return; }

    const newProd = {
        id:         Date.now(),
        name:       name,
        categoryId: catId,
        stock:      parseFloat(document.getElementById('newProdStock').value) || 0,
        lastPrice:  0,
        unit:       document.getElementById('newProdUnit').value
    };
    PRODUCTS_DATA.push(newProd);
    closeModal('addProductModal');
    updateProductSelectOptions();
    showToast('Product "' + name + '" added!', 'success');

    // Reset fields
    ['newProdName', 'newProdSku'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('newProdStock').value = '0';
}

// ============================================================
//  MODAL HELPERS
// ============================================================
function openModal(id)  { document.getElementById(id).classList.add('open');    }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

// ============================================================
//  TOAST
// ============================================================
function showToast(msg, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast     = document.createElement('div');
    toast.className = 'toast ' + type;
    const icons = { info: 'ℹ️', success: '✅', warning: '⚠️', error: '❌' };
    toast.innerHTML = `<span>${icons[type] || 'ℹ️'}</span><span>${msg}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity    = '0';
        toast.style.transition = 'opacity 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ============================================================
//  KEYBOARD: ENTER → focus next input / add row
// ============================================================
function handleEnterKey(e, input) {
    if (e.key !== 'Enter') return;
    e.preventDefault();
    const allInputs = Array.from(document.querySelectorAll('.table-input, .form-control'));
    const idx = allInputs.indexOf(input);
    if (idx > -1 && idx < allInputs.length - 1) {
        allInputs[idx + 1].focus();
    } else {
        addItemRow();
    }
}
