/* =============================================================
   product.js — eLoan SaaS | Product Module
   Vanilla JS — no jQuery required
   ============================================================= */

'use strict';

/* ── Bootstrap Tooltips ─────────────────────────────────────── */
function initTooltips() {
  const tooltipEls = document.querySelectorAll('[data-bs-toggle="tooltip"]');
  tooltipEls.forEach(el => {
    new bootstrap.Tooltip(el, { trigger: 'hover', delay: { show: 350, hide: 100 } });
  });
}

/* ── Delete Confirmation Modal ──────────────────────────────── */
function openDeleteModal(triggerEl) {
  const productId   = triggerEl.getAttribute('data-product-id');
  const productName = triggerEl.getAttribute('data-product-name');

  const nameEl = document.getElementById('deleteProductName');
  const form   = document.getElementById('deleteForm');

  if (nameEl)  nameEl.textContent = productName || 'this product';
  if (form)    form.action        = `/inventory/product/${productId}/delete`;

  const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('deleteModal'));
  modal.show();
}

/* ── Loading Button State ───────────────────────────────────── */
function initLoadingButton() {
  const form    = document.getElementById('productForm');
  const saveBtn = document.getElementById('saveBtn');
  if (!form || !saveBtn) return;

  form.addEventListener('submit', function (e) {
    if (!form.checkValidity()) return; // let HTML5 validation fire
    const defaultText = saveBtn.querySelector('.btn-default-text');
    const loadingText = saveBtn.querySelector('.btn-loading-text');
    if (defaultText) defaultText.classList.add('d-none');
    if (loadingText) loadingText.classList.remove('d-none');
    saveBtn.disabled = true;
  });
}

/* ── Margin Indicator ───────────────────────────────────────── */
function initMarginIndicator() {
  const purchaseInput = document.getElementById('purchasePrice');
  const sellingInput  = document.getElementById('sellingPrice');
  const indicator     = document.getElementById('marginIndicator');
  const marginText    = document.getElementById('marginText');

  if (!purchaseInput || !sellingInput || !indicator) return;

  function updateMargin() {
    const purchase = parseFloat(purchaseInput.value) || 0;
    const selling  = parseFloat(sellingInput.value)  || 0;

    if (purchase > 0 && selling > 0) {
      const margin  = selling - purchase;
      const percent = ((margin / purchase) * 100).toFixed(1);
      const symbol  = margin >= 0 ? '+' : '';

      indicator.style.display = 'flex';

      if (margin < 0) {
        indicator.style.background = 'var(--eloan-red-light)';
        indicator.style.color      = 'var(--eloan-red)';
        indicator.style.borderColor= '#fecaca';
        marginText.textContent = `Selling below cost — loss of ৳${Math.abs(margin).toFixed(2)} (${percent}%)`;
      } else {
        indicator.style.background = 'var(--eloan-green-light)';
        indicator.style.color      = 'var(--eloan-green)';
        indicator.style.borderColor= '#a7f3d0';
        marginText.textContent = `Gross margin: ${symbol}৳${margin.toFixed(2)} (${symbol}${percent}%)`;
      }
    } else {
      indicator.style.display = 'none';
    }
  }

  purchaseInput.addEventListener('input', updateMargin);
  sellingInput.addEventListener('input',  updateMargin);
  updateMargin(); // run on load (for edit page)
}

/* ── Form Validation UX ─────────────────────────────────────── */
function initFormValidation() {
  const form = document.getElementById('productForm');
  if (!form) return;

  // Show custom validation styling on submit
  form.addEventListener('submit', function () {
    form.classList.add('was-validated');
  });

  // Real-time field feedback
  const inputs = form.querySelectorAll('.form-control-eloan, .form-select');
  inputs.forEach(input => {
    input.addEventListener('blur', function () {
      if (this.required && !this.value.trim()) {
        this.classList.add('is-invalid');
        this.classList.remove('is-valid');
      } else if (this.value.trim()) {
        this.classList.remove('is-invalid');
        this.classList.add('is-valid');
      }
    });

    input.addEventListener('input', function () {
      if (this.value.trim()) {
        this.classList.remove('is-invalid');
      }
    });
  });
}

/* ── Search Auto-submit Debounce ────────────────────────────── */
function initSearchDebounce() {
  const searchInput = document.querySelector('.filter-search__input');
  if (!searchInput) return;

  let debounceTimer;
  const form = searchInput.closest('form');
  if (!form) return;

  searchInput.addEventListener('input', function () {
    clearTimeout(debounceTimer);
    // Only auto-submit after 600ms pause to avoid rapid requests
    debounceTimer = setTimeout(() => {
      // Optionally auto-submit — commented out by default
      // form.submit();
    }, 600);
  });
}

/* ── Smooth Row Entry Animation ─────────────────────────────── */
function initTableAnimations() {
  const rows = document.querySelectorAll('.eloan-table__row');
  rows.forEach((row, i) => {
    row.style.opacity    = '0';
    row.style.transform  = 'translateY(6px)';
    row.style.transition = `opacity .25s ease ${i * 35}ms, transform .25s ease ${i * 35}ms`;
    requestAnimationFrame(() => {
      row.style.opacity   = '1';
      row.style.transform = 'translateY(0)';
    });
  });
}

/* ── Stat Card Counter Animation ────────────────────────────── */
function animateCounter(el) {
  const target = parseInt(el.textContent, 10);
  if (isNaN(target) || target === 0) return;
  const duration  = 800;
  const stepTime  = Math.max(Math.floor(duration / target), 16);
  const start     = performance.now();

  function update(now) {
    const elapsed  = now - start;
    const progress = Math.min(elapsed / duration, 1);
    const ease     = 1 - Math.pow(1 - progress, 3);
    el.textContent = Math.round(ease * target);
    if (progress < 1) requestAnimationFrame(update);
  }

  el.textContent = '0';
  requestAnimationFrame(update);
}

function initStatCounters() {
  document.querySelectorAll('.stat-card__value').forEach(el => {
    // IntersectionObserver for in-viewport trigger
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          animateCounter(el);
          observer.unobserve(el);
        }
      });
    }, { threshold: .5 });
    observer.observe(el);
  });
}

/* ── Sidebar Toggle (mobile) ────────────────────────────────── */
function initSidebarToggle() {
  const toggleBtn = document.getElementById('sidebarToggle');
  const sidebar   = document.querySelector('.sidebar') || document.getElementById('sidebar');
  const overlay   = document.getElementById('sidebarOverlay');

  if (!toggleBtn || !sidebar) return;

  toggleBtn.addEventListener('click', function () {
    sidebar.classList.toggle('sidebar--open');
    if (overlay) overlay.classList.toggle('d-none');
  });

  if (overlay) {
    overlay.addEventListener('click', function () {
      sidebar.classList.remove('sidebar--open');
      overlay.classList.add('d-none');
    });
  }
}

/* ── Active Nav Highlight ───────────────────────────────────── */
function highlightActiveNav() {
  const currentPath = window.location.pathname;
  document.querySelectorAll('.nav-link, .sidebar-link').forEach(link => {
    if (link.getAttribute('href') && currentPath.startsWith(link.getAttribute('href'))) {
      link.classList.add('active');
    }
  });
}

/* ── Auto-dismiss Flash Alerts ──────────────────────────────── */
function initFlashAlerts() {
  document.querySelectorAll('.alert-dismissible.auto-dismiss').forEach(alert => {
    setTimeout(() => {
      const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
      if (bsAlert) bsAlert.close();
    }, 4000);
  });
}

/* ── SKU Generator Helper ───────────────────────────────────── */
function initSkuHelper() {
  const nameInput = document.getElementById('name');
  const skuInput  = document.getElementById('sku');
  if (!nameInput || !skuInput) return;

  // Only auto-fill if SKU is still empty (i.e., new product)
  nameInput.addEventListener('blur', function () {
    if (skuInput.value.trim() !== '') return;
    const slug = nameInput.value
      .trim()
      .toUpperCase()
      .replace(/[^A-Z0-9\s]/g, '')
      .replace(/\s+/g, '-')
      .substring(0, 8);
    if (slug) {
      const rand = Math.floor(1000 + Math.random() * 9000);
      skuInput.value = `${slug}-${rand}`;
      skuInput.classList.add('is-valid');
    }
  });
}

/* ── Bootstrap Dropdown Hover (desktop) ────────────────────── */
function initDropdownHover() {
  if (window.innerWidth < 992) return;
  document.querySelectorAll('.dropdown').forEach(dropdown => {
    dropdown.addEventListener('mouseenter', function () {
      const menu = this.querySelector('.dropdown-menu');
      if (menu) menu.classList.add('show');
    });
    dropdown.addEventListener('mouseleave', function () {
      const menu = this.querySelector('.dropdown-menu');
      if (menu) menu.classList.remove('show');
    });
  });
}

/* ── Init All ────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', function () {
  initTooltips();
  initLoadingButton();
  initMarginIndicator();
  initFormValidation();
  initSearchDebounce();
  initTableAnimations();
  initStatCounters();
  initSidebarToggle();
  highlightActiveNav();
  initFlashAlerts();
  initSkuHelper();
  initDropdownHover();
});
