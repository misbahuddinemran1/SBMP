/**
 * eLoan SaaS — category.js
 * Inventory Category Module JavaScript
 * Vanilla JS | No dependencies
 */

(function () {
  'use strict';

  /* ============================================================
     SEARCH FILTER (client-side live search)
     ============================================================ */
  const searchInput = document.getElementById('categorySearch');
  const tableBody   = document.getElementById('categoryTableBody');
  const emptyState  = document.getElementById('emptyState');
  const countBadge  = document.getElementById('categoryCount');

  function filterTable() {
    if (!searchInput || !tableBody) return;

    const query  = searchInput.value.trim().toLowerCase();
    const rows   = tableBody.querySelectorAll('tr[data-searchable]');
    let   visible = 0;

    rows.forEach(function (row) {
      const text = row.getAttribute('data-searchable').toLowerCase();
      const show = !query || text.includes(query);
      row.style.display = show ? '' : 'none';
      if (show) visible++;
    });

    // Empty state toggle
    if (emptyState) {
      emptyState.style.display = visible === 0 ? '' : 'none';
    }

    // Update count badge
    if (countBadge) {
      countBadge.textContent = visible;
    }
  }

  if (searchInput) {
    searchInput.addEventListener('input', filterTable);

    // Keyboard shortcut: focus on /
    document.addEventListener('keydown', function (e) {
      if (e.key === '/' && document.activeElement !== searchInput) {
        e.preventDefault();
        searchInput.focus();
      }
    });
  }

  /* ============================================================
     FILTER DROPDOWN (Status filter)
     ============================================================ */
  const statusFilter = document.getElementById('statusFilter');

  function applyStatusFilter() {
    if (!statusFilter || !tableBody) return;

    const value = statusFilter.value.toLowerCase();
    const rows  = tableBody.querySelectorAll('tr[data-searchable]');
    let   visible = 0;

    rows.forEach(function (row) {
      const status = (row.getAttribute('data-status') || '').toLowerCase();
      const show   = !value || status === value;
      row.style.display = show ? '' : 'none';
      if (show) visible++;
    });

    if (emptyState) emptyState.style.display = visible === 0 ? '' : 'none';
    if (countBadge) countBadge.textContent = visible;

    // Reset search when filter changes
    if (searchInput) searchInput.value = '';
  }

  if (statusFilter) statusFilter.addEventListener('change', applyStatusFilter);

  /* ============================================================
     DELETE CONFIRMATION MODAL
     ============================================================ */
  const deleteModal     = document.getElementById('deleteModal');
  const deleteModalName = document.getElementById('deleteTargetName');
  const deleteForm      = document.getElementById('deleteForm');

 function openDeleteModal(categoryId, categoryName) {
   if (!deleteModal) return;

   if (deleteModalName) {
     deleteModalName.textContent =
         categoryName || 'this category';
   }

   if (deleteForm) {

     deleteForm.action =
         '/inventory/category/' +
         categoryId +
         '/delete';

   }

   const modal =
       bootstrap.Modal.getOrCreateInstance(deleteModal);

   modal.show();
 }

  // Attach to all delete buttons
  document.querySelectorAll('.delete-btn').forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      e.preventDefault();
      const id   = btn.getAttribute('data-delete-id');
      const name = btn.getAttribute('data-delete-name');
      openDeleteModal(id, name);
    });
  });

  /* ============================================================
     LOADING BUTTON STATE
     ============================================================ */
  function setButtonLoading(btn, isLoading) {
    if (!btn) return;

    if (isLoading) {
      btn.dataset.originalHtml = btn.innerHTML;
      btn.innerHTML = '<span class="spinner"></span> Saving...';
      btn.classList.add('loading');
      btn.disabled = true;
    } else {
      if (btn.dataset.originalHtml) {
        btn.innerHTML = btn.dataset.originalHtml;
      }
      btn.classList.remove('loading');
      btn.disabled = false;
    }
  }

  /* ============================================================
     FORM VALIDATION (Add / Edit Category)
     ============================================================ */
  const categoryForm   = document.getElementById('categoryForm');
  const saveBtn        = document.getElementById('saveCategoryBtn');
  const nameInput      = document.getElementById('categoryName');
  const nameError      = document.getElementById('nameError');
  const descInput      = document.getElementById('categoryDescription');

  function validateName() {
    if (!nameInput) return true;
    const val = nameInput.value.trim();

    if (!val) {
      showFieldError(nameInput, nameError, 'Category name is required.');
      return false;
    }

    if (val.length < 2) {
      showFieldError(nameInput, nameError, 'Name must be at least 2 characters.');
      return false;
    }

    if (val.length > 100) {
      showFieldError(nameInput, nameError, 'Name cannot exceed 100 characters.');
      return false;
    }

    clearFieldError(nameInput, nameError);
    nameInput.classList.add('is-valid');
    return true;
  }

  function showFieldError(input, errorEl, message) {
    if (input)   { input.classList.remove('is-valid'); input.classList.add('is-invalid'); }
    if (errorEl) { errorEl.textContent = message; errorEl.style.display = 'flex'; }
  }

  function clearFieldError(input, errorEl) {
    if (input)   { input.classList.remove('is-invalid'); }
    if (errorEl) { errorEl.style.display = 'none'; }
  }

  if (nameInput) {
    nameInput.addEventListener('input', function () {
      if (nameInput.value.trim().length > 0) validateName();
      else clearFieldError(nameInput, nameError);
    });

    nameInput.addEventListener('blur', validateName);
  }

  if (categoryForm) {
    categoryForm.addEventListener('submit', function (e) {
      const isValid = validateName();

      if (!isValid) {
        e.preventDefault();
        if (nameInput) nameInput.focus();
        return;
      }

      // Set loading state
      setButtonLoading(saveBtn, true);
    });
  }

  /* ============================================================
     CHARACTER COUNTER for Description
     ============================================================ */
  const descCounter = document.getElementById('descCounter');

  if (descInput && descCounter) {
    const maxLen = parseInt(descInput.getAttribute('maxlength') || '255', 10);

    function updateCounter() {
      const remaining = maxLen - descInput.value.length;
      descCounter.textContent = descInput.value.length + ' / ' + maxLen;
      descCounter.style.color = remaining < 30 ? 'var(--warning)' : 'var(--text-muted)';
    }

    descInput.addEventListener('input', updateCounter);
    updateCounter();
  }

  /* ============================================================
     TOGGLE SWITCH — visual label update
     ============================================================ */
  const statusToggle     = document.getElementById('statusToggle');
  const statusLabelTitle = document.getElementById('statusLabelTitle');
  const statusLabelSub   = document.getElementById('statusLabelSub');

  if (statusToggle) {
    statusToggle.addEventListener('change', function () {
      if (statusLabelTitle) {
        statusLabelTitle.textContent = statusToggle.checked ? 'Active' : 'Inactive';
      }
      if (statusLabelSub) {
        statusLabelSub.textContent = statusToggle.checked
          ? 'Category is visible and usable'
          : 'Category is hidden from the system';
      }
    });
  }

  /* ============================================================
     AUTO-DISMISS FLASH ALERTS
     ============================================================ */
  document.querySelectorAll('.flash-alert[data-auto-dismiss]').forEach(function (alert) {
    const delay = parseInt(alert.getAttribute('data-auto-dismiss') || '4000', 10);
    setTimeout(function () {
      alert.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
      alert.style.opacity    = '0';
      alert.style.transform  = 'translateY(-6px)';
      setTimeout(function () { alert.remove(); }, 400);
    }, delay);
  });

  /* ============================================================
     ANIMATE ROWS on load
     ============================================================ */
  (function animateRows() {
    const rows = document.querySelectorAll('.cat-table tbody tr');
    rows.forEach(function (row, i) {
      row.style.opacity   = '0';
      row.style.transform = 'translateY(8px)';
      setTimeout(function () {
        row.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
        row.style.opacity    = '1';
        row.style.transform  = 'translateY(0)';
      }, 40 + i * 40);
    });
  })();

  /* ============================================================
     Init log
     ============================================================ */
  console.info('[eLoan SaaS] Category module JS initialized ✓');

})();