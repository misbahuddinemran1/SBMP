/**
 * supplier.js – eLoan SaaS | Supplier Module
 * Vanilla JS · Bootstrap 5
 */

(function () {
    'use strict';

    /* =====================================================
       1. DELETE MODAL
       ===================================================== */

    /**
     * Open the delete confirmation modal.
     * @param {HTMLElement} btn – the trigger element with data-id and data-name
     */
    window.openDeleteModal = function (btn) {
        const id   = btn.getAttribute('data-id');
        const name = btn.getAttribute('data-name') || 'this supplier';

        const form = document.getElementById('deleteForm');
        const label = document.getElementById('deleteSupplierName');

        if (form && id) {
            form.action = '/inventory/supplier/' + id + '/delete';
        }

        if (label) {
            label.textContent = name;
        }

        const modalEl = document.getElementById('deleteModal');
        if (modalEl) {
            const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            modal.show();
        }
    };

    /* Delete form loading state */
    document.addEventListener('DOMContentLoaded', function () {
        const deleteForm = document.getElementById('deleteForm');
        if (deleteForm) {
            deleteForm.addEventListener('submit', function () {
                const btn = document.getElementById('deleteConfirmBtn');
                if (btn) {
                    btn.classList.add('btn-loading');
                    btn.disabled = true;
                    const txt = btn.querySelector('.btn-text');
                    const spn = btn.querySelector('.btn-spinner');
                    if (txt) txt.classList.add('d-none');
                    if (spn) spn.classList.remove('d-none');
                }
            });
        }
    });

    /* =====================================================
       2. FORM VALIDATION + LOADING STATE
       ===================================================== */

    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('supplierForm');
        if (!form) return;

        form.addEventListener('submit', function (e) {
            let valid = true;

            /* Required: supplierName */
            const nameInput = form.querySelector('[name="supplierName"]');
            if (nameInput && nameInput.value.trim() === '') {
                nameInput.classList.add('is-invalid');
                valid = false;
            } else if (nameInput) {
                nameInput.classList.remove('is-invalid');
            }

            /* Email format */
            const emailInput = form.querySelector('[name="email"]');
            if (emailInput && emailInput.value.trim() !== '') {
                const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRe.test(emailInput.value.trim())) {
                    emailInput.classList.add('is-invalid');
                    valid = false;
                } else {
                    emailInput.classList.remove('is-invalid');
                }
            }

            if (!valid) {
                e.preventDefault();
                /* Scroll to first invalid input */
                const firstInvalid = form.querySelector('.is-invalid');
                if (firstInvalid) {
                    firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstInvalid.focus();
                }
                return;
            }

            /* Show loading state */
            const saveBtn = document.getElementById('saveBtn');
            if (saveBtn) {
                saveBtn.disabled = true;
                const txt = saveBtn.querySelector('.btn-text');
                const spn = saveBtn.querySelector('.btn-spinner');
                if (txt) txt.classList.add('d-none');
                if (spn) spn.classList.remove('d-none');
            }
        });

        /* Live clear invalid state */
        form.querySelectorAll('.form-control-erp').forEach(function (input) {
            input.addEventListener('input', function () {
                if (this.value.trim() !== '') {
                    this.classList.remove('is-invalid');
                }
            });
        });
    });

    /* =====================================================
       3. SEARCH INPUT – Live clear button behaviour
       ===================================================== */

    document.addEventListener('DOMContentLoaded', function () {
        const searchInput = document.getElementById('supplierSearch');
        if (!searchInput) return;

        /* Highlight on focus */
        searchInput.addEventListener('focus', function () {
            this.select();
        });

        /* Submit form on Enter (already works natively, this is a UX boost) */
        searchInput.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                this.value = '';
                this.form && this.form.submit();
            }
        });
    });

    /* =====================================================
       4. SIDEBAR RESPONSIVE TOGGLE
       ===================================================== */

    document.addEventListener('DOMContentLoaded', function () {
        /* Some dashboard layouts inject a sidebar toggle button.
           This listens for a common pattern of toggling .sidebar-collapsed on <body> */
        const sidebarToggle = document.getElementById('sidebarToggle');
        if (sidebarToggle) {
            sidebarToggle.addEventListener('click', function () {
                document.body.classList.toggle('sidebar-collapsed');
            });
        }
    });

    /* =====================================================
       5. TABLE ROW CLICK – Navigate to view page
           (optional: only if not clicking an action button)
       ===================================================== */

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.table-row-hover').forEach(function (row) {
            row.style.cursor = 'default';
        });
    });

    /* =====================================================
       6. SMOOTH PAGE ENTRANCE ANIMATION
       ===================================================== */

    document.addEventListener('DOMContentLoaded', function () {
        const content = document.querySelector('.erp-content');
        if (content) {
            content.style.opacity  = '0';
            content.style.transform = 'translateY(8px)';
            content.style.transition = 'opacity 0.3s ease, transform 0.3s ease';

            requestAnimationFrame(function () {
                requestAnimationFrame(function () {
                    content.style.opacity  = '1';
                    content.style.transform = 'translateY(0)';
                });
            });
        }
    });

    /* =====================================================
       7. STAT CARDS COUNTER ANIMATION
       ===================================================== */

    document.addEventListener('DOMContentLoaded', function () {
        function animateCounter(el) {
            const target = parseInt(el.textContent, 10);
            if (isNaN(target) || target === 0) return;

            let current = 0;
            const step  = Math.max(1, Math.floor(target / 30));
            const timer = setInterval(function () {
                current += step;
                if (current >= target) {
                    current = target;
                    clearInterval(timer);
                }
                el.textContent = current;
            }, 28);
        }

        document.querySelectorAll('.stat-card__value').forEach(animateCounter);
    });

    /* =====================================================
       8. AUTO-DISMISS FLASH ALERTS (if present)
       ===================================================== */

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.alert-dismissible[data-auto-dismiss]').forEach(function (alert) {
            const delay = parseInt(alert.getAttribute('data-auto-dismiss'), 10) || 4000;
            setTimeout(function () {
                const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
                bsAlert.close();
            }, delay);
        });
    });

})();
