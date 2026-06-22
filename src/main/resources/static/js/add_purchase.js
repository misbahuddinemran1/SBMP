let globalCategories = [];
let globalProducts = [];

let purchaseItemCount = 0;
let paymentRowCount = 0;

let activeCategoryFilter = 'ALL';
let activeModalTriggerIndex = null;


/* =========================================================
   DOM READY
========================================================= */
document.addEventListener('DOMContentLoaded', () => {

    // CATEGORY DATA
    if (typeof initialCategories !== 'undefined') {

        globalCategories = initialCategories.map(c => ({
            id: c.id,
            name: c.name,
            code: c.code || '',
            description: c.description || ''
        }));
    }

    // PRODUCT DATA
    if (typeof initialProducts !== 'undefined') {

        globalProducts = initialProducts.map(p => ({
            id: p.id,
            name: p.name,
            categoryId: p.category?.id || null,
            stockQuantity: p.stockQuantity || 0,
            purchasePrice: p.purchasePrice || 0
        }));
    }

    initializeSupplierCardTrigger();
    initializeCategoryCardTrigger();

    addPurchaseRow();
    addPaymentRow();

    const invoiceDiscountInput =
        document.getElementById('invoice-discount-input');

    if (invoiceDiscountInput) {

        invoiceDiscountInput.addEventListener(
            'input',
            recalculateEntireVoucher
        );
    }
});


/* =========================================================
   SUPPLIER CARD
========================================================= */
function initializeSupplierCardTrigger() {

    const select =
        document.getElementById('supplier-select');

    if (!select) return;

    select.addEventListener('change', () => {

        const option =
            select.options[select.selectedIndex];

        const card =
            document.getElementById('supplier-card');

        if (!option || option.value === "") {

            card?.classList.add('hidden');
            return;
        }

        document.getElementById('sup-company').innerText =
            option.getAttribute('data-company') || 'N/A';

        document.getElementById('sup-phone').innerText =
            option.getAttribute('data-phone') || 'N/A';

        document.getElementById('sup-address').innerText =
            option.getAttribute('data-address') || 'N/A';

        card?.classList.remove('hidden');
    });
}


/* =========================================================
   CATEGORY CARD
========================================================= */
function initializeCategoryCardTrigger() {

    const select =
        document.getElementById('category-select');

    if (!select) return;

    select.addEventListener('change', () => {

        const option =
            select.options[select.selectedIndex];

        const card =
            document.getElementById('category-card');

        if (!option || option.value === "") {

            card?.classList.add('hidden');
            return;
        }

        document.getElementById('cat-name').innerText =
            option.text || 'N/A';

        document.getElementById('cat-code').innerText =
            option.getAttribute('data-code') || 'N/A';

        document.getElementById('cat-description').innerText =
            option.getAttribute('data-description') || 'No Description';

        card?.classList.remove('hidden');
    });
}


/* =========================================================
   CATEGORY CHIP FILTER
========================================================= */
function selectCategoryChip(categoryId) {

    activeCategoryFilter = categoryId.toString();

    document.querySelectorAll('.category-chip')
        .forEach(chip => {

            if (
                chip.getAttribute('data-category-id')
                === activeCategoryFilter
            ) {

                chip.className =
                    "category-chip px-3 py-1.5 rounded-xl text-xs font-bold bg-indigo-600 text-white border border-indigo-600 shadow-sm transition-all duration-150";

            } else {

                chip.className =
                    "category-chip px-3 py-1.5 rounded-xl text-xs font-bold bg-slate-50 text-slate-600 border border-slate-200 hover:bg-slate-100 transition-all duration-150";
            }
        });

    filterAllProductDropdowns();
}


/* =========================================================
   FILTER ALL PRODUCT DROPDOWNS
========================================================= */
function filterAllProductDropdowns() {

    document.querySelectorAll('.row-product-select')
        .forEach(select => {

            const index =
                select.getAttribute('data-row-index');

            const catSelect =
                document.getElementById(`cat-select-${index}`);

            let targetCatId =
                activeCategoryFilter;

            if (
                targetCatId === 'ALL' &&
                catSelect &&
                catSelect.value !== ""
            ) {

                targetCatId = catSelect.value;
            }

            const currentSelectedValue =
                select.value;

            select.innerHTML =
                '<option value="">-- Search Product --</option>';

            const filteredProducts =
                globalProducts.filter(p => {

                    return (
                        targetCatId === 'ALL' ||
                        (
                            p.categoryId &&
                            p.categoryId.toString() === targetCatId
                        )
                    );
                });

            filteredProducts.forEach(product => {

                const option =
                    document.createElement('option');

                option.value = product.id;
                option.text = product.name;

                option.setAttribute(
                    'data-category-id',
                    product.categoryId
                );

                option.setAttribute(
                    'data-stock',
                    product.stockQuantity
                );

                option.setAttribute(
                    'data-price',
                    product.purchasePrice
                );

                select.appendChild(option);
            });

            const modalOption =
                document.createElement('option');

            modalOption.value = "NEW_MODAL";
            modalOption.text = "+ Add Product";

            select.appendChild(modalOption);

            if (
                [...select.options]
                    .some(o => o.value == currentSelectedValue)
            ) {

                select.value = currentSelectedValue;
            }
        });
}


/* =========================================================
   ADD PURCHASE ROW
========================================================= */
function addPurchaseRow() {

    const tbody =
        document.getElementById('purchase-tbody');

    if (!tbody) return;

    const index =
        purchaseItemCount++;

    const tr =
        document.createElement('tr');

    tr.id = `purchase-row-${index}`;

    tr.className =
        'hover:bg-slate-50/80 transition-colors animate-fadeIn duration-200 relative group';

    tr.innerHTML = `
    
        <td class="py-2 px-4 border border-slate-100">

             <select id="cat-select-${index}"
        name="items[${index}].categoryId"
        class="row-category-select w-full bg-transparent text-xs font-semibold text-slate-600 outline-none p-1.5 rounded"
        onchange="onRowCategoryChange(${index})">

                <option value="">-- Category --</option>

                ${globalCategories.map(c => `
                    <option value="${c.id}">
                        ${c.name}
                    </option>
                `).join('')}

            </select>

        </td>

        <td class="py-2 px-4 border border-slate-100">

            <select id="prod-select-${index}"
                    name="items[${index}].productId"
                    data-row-index="${index}"
                    required
                    class="row-product-select w-full bg-transparent text-xs font-bold text-slate-700 outline-none p-1.5 rounded"
                    onchange="onRowProductChange(${index})">

                <option value="">-- Search Product --</option>

                ${globalProducts.map(p => `
                    <option value="${p.id}"
                            data-category-id="${p.categoryId}"
                            data-stock="${p.stockQuantity}"
                            data-price="${p.purchasePrice}">
                        ${p.name}
                    </option>
                `).join('')}

                <option value="NEW_MODAL">
                    + Add Product
                </option>

            </select>

        </td>

        <td class="py-2 px-3 text-center border border-slate-100">

            <span id="badge-stock-${index}"
                  class="inline-block px-2 py-1 bg-slate-200 text-[10px] rounded">
                STK: 0
            </span>

            <span id="badge-price-${index}"
                  class="inline-block px-2 py-1 bg-slate-200 text-[10px] rounded block mt-1">
                LST: $0.00
            </span>

        </td>

        <td class="py-2 px-3 border border-slate-100">

            <input type="number"
                   id="qty-input-${index}"
                   name="items[${index}].purchaseQty"
                   value="1"
                   min="1"
                   required
                   class="keyboard-nav-target w-full text-center text-xs outline-none"
                   oninput="calculateRowTotal(${index})">

        </td>

        <td class="py-2 px-3 border border-slate-100">

            <input type="number"
                   id="price-input-${index}"
                   name="items[${index}].unitPrice"
                   value="0.00"
                   min="0"
                   step="any"
                   required
                   class="keyboard-nav-target w-full text-right text-xs outline-none"
                   oninput="calculateRowTotal(${index})">

        </td>

        <td class="py-2 px-3 border border-slate-100">

            <input type="number"
                   id="disc-amt-input-${index}"
                   name="items[${index}].discountAmount"
                   value="0.00"
                   min="0"
                   step="any"
                   class="keyboard-nav-target w-full text-right text-xs outline-none"
                   oninput="handleDiscountToggle(${index}, 'AMOUNT')">

        </td>

        <td class="py-2 px-3 border border-slate-100">

            <input type="number"
                   id="disc-pct-input-${index}"
                   name="items[${index}].discountPercentage"
                   value="0.00"
                   min="0"
                   max="100"
                   step="any"
                   class="keyboard-nav-target w-full text-right text-xs outline-none"
                   oninput="handleDiscountToggle(${index}, 'PERCENTAGE')">

        </td>

        <td class="py-2 px-4 text-right border border-slate-100">

            <span id="line-total-${index}"
                  class="line-net-span text-xs font-bold font-mono"
                  data-subtotal="0"
                  data-discount="0">

                $0.00

            </span>

        </td>

        <td class="py-2 px-3 text-center border border-slate-100">

            <button type="button"
                    onclick="removePurchaseRow(${index})"
                    class="text-slate-400 hover:text-rose-500">

                ✕
            </button>

        </td>
    `;

    tbody.appendChild(tr);

    attachKeyboardNavigation(tr);

    toggleTableEmptyState();

    recalculateEntireVoucher();
}


/* =========================================================
   REMOVE PURCHASE ROW
========================================================= */
function removePurchaseRow(index) {

    const rows =
        document.querySelectorAll('#purchase-tbody tr');

    if (rows.length === 1) {

        alert("Minimum one row required.");
        return;
    }

    const row =
        document.getElementById(`purchase-row-${index}`);

    row?.remove();

    toggleTableEmptyState();

    recalculateEntireVoucher();
}


/* =========================================================
   EMPTY STATE
========================================================= */
function toggleTableEmptyState() {

    const tbody =
        document.getElementById('purchase-tbody');

    const emptyState =
        document.getElementById('table-empty-state');

    if (!tbody || !emptyState) return;

    emptyState.className =
        tbody.children.length === 0
            ? "py-12 flex flex-col items-center justify-center text-center"
            : "hidden";
}


/* =========================================================
   ROW CATEGORY CHANGE
========================================================= */
function onRowCategoryChange(index) {

    const categoryValue =
        document.getElementById(`cat-select-${index}`).value;

    const productSelect =
        document.getElementById(`prod-select-${index}`);

    if (!productSelect) return;

    productSelect.innerHTML =
        '<option value="">-- Search Product --</option>';

    const filteredProducts =
        globalProducts.filter(p => {

            return (
                categoryValue === "" ||
                (
                    p.categoryId &&
                    p.categoryId.toString() === categoryValue
                )
            );
        });

    filteredProducts.forEach(product => {

        const option =
            document.createElement('option');

        option.value = product.id;
        option.text = product.name;

        option.setAttribute(
            'data-category-id',
            product.categoryId
        );

        option.setAttribute(
            'data-stock',
            product.stockQuantity
        );

        option.setAttribute(
            'data-price',
            product.purchasePrice
        );

        productSelect.appendChild(option);
    });

    productSelect.innerHTML += `
        <option value="NEW_MODAL">
            + Add Product
        </option>
    `;

    resetRowMetrics(index);
}


/* =========================================================
   PRODUCT CHANGE
========================================================= */
function onRowProductChange(index) {

    const productSelect =
        document.getElementById(`prod-select-${index}`);

    if (!productSelect) return;

    if (productSelect.value === "NEW_MODAL") {

        activeModalTriggerIndex = index;

        openProductModal();

        productSelect.value = "";

        return;
    }

    const option =
        productSelect.options[productSelect.selectedIndex];

    if (!option || !productSelect.value) {

        resetRowMetrics(index);
        return;
    }

    const stock =
        option.getAttribute('data-stock') || 0;

    const price =
        option.getAttribute('data-price') || 0;

    const categoryId =
        option.getAttribute('data-category-id');

    document.getElementById(`badge-stock-${index}`).innerText =
        `STK: ${stock}`;

    document.getElementById(`badge-price-${index}`).innerText =
        `LST: $${parseFloat(price).toFixed(2)}`;

    document.getElementById(`price-input-${index}`).value =
        parseFloat(price).toFixed(2);

    if (categoryId && categoryId !== "null") {

        document.getElementById(`cat-select-${index}`).value =
            categoryId;
    }

    calculateRowTotal(index);
}


/* =========================================================
   RESET ROW
========================================================= */
function resetRowMetrics(index) {

    document.getElementById(`badge-stock-${index}`).innerText =
        "STK: 0";

    document.getElementById(`badge-price-${index}`).innerText =
        "LST: $0.00";

    document.getElementById(`qty-input-${index}`).value =
        "1";

    document.getElementById(`price-input-${index}`).value =
        "0.00";

    document.getElementById(`line-total-${index}`).innerText =
        "$0.00";

    document.getElementById(`line-total-${index}`)
        .setAttribute('data-subtotal', '0');

    document.getElementById(`line-total-${index}`)
        .setAttribute('data-discount', '0');

    recalculateEntireVoucher();
}


/* =========================================================
   DISCOUNT TOGGLE
========================================================= */
function handleDiscountToggle(index, triggerType) {

    const amountInput =
        document.getElementById(`disc-amt-input-${index}`);

    const percentInput =
        document.getElementById(`disc-pct-input-${index}`);

    if (
        triggerType === 'AMOUNT' &&
        parseFloat(amountInput.value) > 0
    ) {

        percentInput.value = "0.00";
    }

    if (
        triggerType === 'PERCENTAGE' &&
        parseFloat(percentInput.value) > 0
    ) {

        amountInput.value = "0.00";
    }

    calculateRowTotal(index);
}


/* =========================================================
   CALCULATE ROW TOTAL
========================================================= */
function calculateRowTotal(index) {

    const qty =
        parseFloat(
            document.getElementById(`qty-input-${index}`).value
        ) || 0;

    const price =
        parseFloat(
            document.getElementById(`price-input-${index}`).value
        ) || 0;

    const discountAmount =
        parseFloat(
            document.getElementById(`disc-amt-input-${index}`).value
        ) || 0;

    const discountPercent =
        parseFloat(
            document.getElementById(`disc-pct-input-${index}`).value
        ) || 0;

    const subtotal =
        qty * price;

    let effectiveDiscount = 0;

    if (discountAmount > 0) {

        effectiveDiscount = discountAmount;

    } else if (discountPercent > 0) {

        effectiveDiscount =
            subtotal * (discountPercent / 100);
    }

    effectiveDiscount =
        Math.min(effectiveDiscount, subtotal);

    const netTotal =
        Math.max(0, subtotal - effectiveDiscount);

    const totalSpan =
        document.getElementById(`line-total-${index}`);

    totalSpan.innerText =
        `$${netTotal.toFixed(2)}`;

    totalSpan.setAttribute(
        'data-subtotal',
        subtotal.toString()
    );

    totalSpan.setAttribute(
        'data-discount',
        effectiveDiscount.toString()
    );

    recalculateEntireVoucher();
}


/* =========================================================
   RECALCULATE VOUCHER
========================================================= */
function recalculateEntireVoucher() {

    let subtotal = 0;
    let totalDiscount = 0;

    document.querySelectorAll('.line-net-span')
        .forEach(span => {

            subtotal +=
                parseFloat(
                    span.getAttribute('data-subtotal')
                ) || 0;

            totalDiscount +=
                parseFloat(
                    span.getAttribute('data-discount')
                ) || 0;
        });

    const invoiceDiscount =
        parseFloat(
            document.getElementById('invoice-discount-input')?.value
        ) || 0;

    const grandTotal =
        Math.max(
            0,
            subtotal - totalDiscount - invoiceDiscount
        );

    document.getElementById('summary-subtotal').innerText =
        `$${subtotal.toFixed(2)}`;

    document.getElementById('summary-discount').innerText =
        `-$${(totalDiscount + invoiceDiscount).toFixed(2)}`;

    document.getElementById('summary-grand-total').innerText =
        `$${grandTotal.toFixed(2)}`;

    recalculatePayments(grandTotal);
}


/* =========================================================
   PAYMENT
========================================================= */
function addPaymentRow() {

    const container =
        document.getElementById('payment-matrix-container');

    if (!container) return;

    const index =
        paymentRowCount++;

    const div =
        document.createElement('div');

    div.id = `payment-row-${index}`;

    div.className =
        'grid grid-cols-1 md:grid-cols-4 gap-4 bg-slate-50 p-4 rounded-xl border border-slate-100';

    div.innerHTML = `

        <div>

            <label class="text-[10px] font-bold uppercase">
                Method
            </label>

            <select name="payments[${index}].paymentMethod"
                    class="w-full border rounded-lg p-2 text-xs">

                <option value="CASH">Cash</option>
                <option value="BANK">Bank</option>
                <option value="BKASH">bKash</option>
                <option value="NAGAD">Nagad</option>

            </select>

        </div>

        <div>

            <label class="text-[10px] font-bold uppercase">
                Amount
            </label>

            <input type="number"
                   name="payments[${index}].amount"
                   value="0.00"
                   min="0"
                   step="any"
                   class="payment-amount-input w-full border rounded-lg p-2 text-xs"
                   oninput="recalculateEntireVoucher()">

        </div>

        <div>

            <label class="text-[10px] font-bold uppercase">
                Payment Date
            </label>

            <input type="date"
                   name="payments[${index}].paymentDate"
                   value="${new Date().toISOString().split('T')[0]}"
                   class="w-full border rounded-lg p-2 text-xs"
                   required>

        </div>

        <div class="relative">

            <label class="text-[10px] font-bold uppercase">
                Reference
            </label>

            <input type="text"
                   name="payments[${index}].referenceNo"
                   placeholder="Txn ID"
                   class="w-full border rounded-lg p-2 text-xs">

            <button type="button"
                    onclick="removePaymentRow(${index})"
                    class="absolute top-8 right-2 text-rose-500">

                ✕

            </button>

        </div>
    `;

    container.appendChild(div);
}


function removePaymentRow(index) {

    const row =
        document.getElementById(`payment-row-${index}`);

    row?.remove();

    recalculateEntireVoucher();
}


function recalculatePayments(grandTotal) {

    let paymentTotal = 0;

    document.querySelectorAll('.payment-amount-input')
        .forEach(input => {

            paymentTotal +=
                parseFloat(input.value) || 0;
        });

    const advancePaid =
        parseFloat(
            document.getElementById('advance-paid-input')?.value
        ) || 0;

    const totalPaid =
        paymentTotal + advancePaid;

    const advanceElement =
        document.getElementById('summary-advance-paid');

    if (advanceElement) {
        advanceElement.innerText =
            `৳${advancePaid.toFixed(2)}`;
    }

    const paidElement =
        document.getElementById('summary-paid');

    if (paidElement) {
        paidElement.innerText =
            `৳${totalPaid.toFixed(2)}`;
    }

    const dueElement =
        document.getElementById('summary-due');

    if (dueElement) {
        dueElement.innerText =
            `৳${Math.max(0, grandTotal - totalPaid).toFixed(2)}`;
    }
}


/* =========================================================
   KEYBOARD NAVIGATION
========================================================= */
function attachKeyboardNavigation(rowElement) {

    const targets =
        rowElement.querySelectorAll('.keyboard-nav-target');

    targets.forEach((input, index) => {

        input.addEventListener('keydown', e => {

            if (e.key !== 'Enter') return;

            e.preventDefault();

            if (index < targets.length - 1) {

                targets[index + 1].focus();
                targets[index + 1].select();

            } else {

                addPurchaseRow();

                setTimeout(() => {

                    const lastRow =
                        document.querySelector(
                            '#purchase-tbody tr:last-child'
                        );

                    const nextInput =
                        lastRow?.querySelector(
                            '.keyboard-nav-target'
                        );

                    nextInput?.focus();
                    nextInput?.select();

                }, 50);
            }
        });
    });
}


/* =========================================================
   MODAL
========================================================= */
function toggleModal(id, open) {

    const modal =
        document.getElementById(id);

    if (!modal) return;

    const box =
        modal.querySelector('div');

    if (!box) return;

    if (open) {

        modal.classList.remove('hidden');

        setTimeout(() => {

            modal.classList.remove('opacity-0');
            box.classList.remove('scale-95');

        }, 10);

    } else {

        modal.classList.add('opacity-0');
        box.classList.add('scale-95');

        setTimeout(() => {

            modal.classList.add('hidden');

        }, 300);
    }
}


function openProductModal() {
    toggleModal('product-modal', true);
}

function closeProductModal() {
    toggleModal('product-modal', false);
}

function openCategoryModal() {
    toggleModal('category-modal', true);
}

function closeCategoryModal() {
    toggleModal('category-modal', false);
}


/* =========================================================
   SAVE CATEGORY
========================================================= */
async function saveCategoryModal() {

    const nameInput =
        document.getElementById('modal-category-name');

    const name =
        nameInput.value.trim();

    if (!name) {

        alert("Category name required.");
        return;
    }

    try {

        const response =
            await fetch('/api/categories/create', {

                method: 'POST',

                headers: {
                    'Content-Type': 'application/json'
                },

                body: JSON.stringify({
                    name
                })
            });

        if (!response.ok) {

            throw new Error('Failed');
        }

        const data =
            await response.json();

        globalCategories.push({
            id: data.id,
            name: data.name
        });

        const chipContainer =
            document.getElementById('category-chips-container');

        const button =
            document.createElement('button');

        button.type = "button";

        button.innerText = data.name;

        button.setAttribute(
            'data-category-id',
            data.id
        );

        button.className =
            "category-chip px-3 py-1.5 rounded-xl text-xs font-bold bg-slate-50 text-slate-600 border border-slate-200 hover:bg-slate-100 transition-all duration-150";

        button.onclick = () =>
            selectCategoryChip(data.id);

        chipContainer?.appendChild(button);

        document.querySelectorAll('.row-category-select')
            .forEach(select => {

                select.innerHTML += `
                    <option value="${data.id}">
                        ${data.name}
                    </option>
                `;
            });

        nameInput.value = "";

        closeCategoryModal();

    } catch (error) {

        console.error(error);

        alert("Failed to save category.");
    }
}


/* =========================================================
   SAVE PRODUCT
========================================================= */
async function saveProductModal() {

    const nameInput =
        document.getElementById('modal-product-name');

    const categorySelect =
        document.getElementById('modal-product-category');

    if (
        !nameInput.value.trim() ||
        !categorySelect.value
    ) {

        alert("Complete all fields.");
        return;
    }

    try {

        const response =
            await fetch('/api/products/create', {

                method: 'POST',

                headers: {
                    'Content-Type': 'application/json'
                },

                body: JSON.stringify({

                    name: nameInput.value.trim(),

                    categoryId: categorySelect.value
                })
            });

        if (!response.ok) {

            throw new Error('Failed');
        }

        const data =
            await response.json();

        globalProducts.push({

            id: data.id,
            name: data.name,
            categoryId: data.categoryId,
            stockQuantity: 0,
            purchasePrice: 0
        });

        filterAllProductDropdowns();

        if (activeModalTriggerIndex !== null) {

            const productSelect =
                document.getElementById(
                    `prod-select-${activeModalTriggerIndex}`
                );

            if (productSelect) {

                productSelect.value = data.id;

                onRowProductChange(
                    activeModalTriggerIndex
                );
            }

            activeModalTriggerIndex = null;
        }

        nameInput.value = "";
        categorySelect.value = "";

        closeProductModal();

    } catch (error) {

        console.error(error);

        alert("Failed to save product.");
    }
}


/* =========================================================
   VALIDATION GLOW
========================================================= */
function applyValidationGlow(element) {

    if (!element) return;

    element.classList.add(
        'animate-shake',
        'border-rose-500'
    );

    setTimeout(() => {

        element.classList.remove(
            'animate-shake'
        );

    }, 400);
}


/* =========================================================
   SUBMIT FORM
========================================================= */
function submitPurchaseForm(status) {

    const supplierSelect =
        document.getElementById('supplier-select');

    if (!supplierSelect.value) {

        applyValidationGlow(supplierSelect);

        alert("Please select supplier.");

        return;
    }

    const tbody =
        document.getElementById('purchase-tbody');

    if (!tbody || tbody.children.length === 0) {

        alert("No purchase items found.");
        return;
    }

    let validationPassed = true;

    tbody.querySelectorAll('.row-product-select')
        .forEach(select => {

            if (
                !select.value ||
                select.value === "NEW_MODAL"
            ) {

                applyValidationGlow(select);

                validationPassed = false;
            }
        });

    if (!validationPassed) {

        alert("Please select valid products.");
        return;
    }

    document.getElementById('purchase-status').value =
        status;

    const invoiceDiscount =
        parseFloat(
            document.getElementById(
                'invoice-discount-input'
            )?.value
        ) || 0;

    const hiddenDiscountField =
        document.getElementById(
            'invoice-discount-hidden'
        );

    if (hiddenDiscountField) {

        hiddenDiscountField.value =
            invoiceDiscount;
    }

    document.getElementById('purchase-form')
        ?.submit();
}