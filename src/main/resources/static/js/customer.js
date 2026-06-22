/**
 * eLoan SaaS - Customer Module JavaScript
 * Enterprise ERP | Vanilla JS | Bootstrap 5
 */

'use strict';

/* ============================================================
   UTILITIES
   ============================================================ */
const Utils = {
  debounce(fn, delay = 300) {
    let t;
    return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), delay); };
  },

  setButtonLoading(btn, loading) {
    if (!btn) return;
    if (loading) {
      btn.classList.add('btn-loading');
      btn.disabled = true;
    } else {
      btn.classList.remove('btn-loading');
      btn.disabled = false;
    }
  },

  showAlert(message, type = 'success', container = null) {
    const alertEl = document.createElement('div');
    alertEl.className = `alert-custom alert-custom-${type} mb-3`;
    alertEl.innerHTML = `<i class="bi bi-${type === 'success' ? 'check-circle-fill' : 'exclamation-circle-fill'}"></i> ${message}`;
    const target = container || document.querySelector('.page-content');
    if (target) {
      target.prepend(alertEl);
      setTimeout(() => alertEl.remove(), 5000);
    }
  },

  getInitials(name) {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/);
    return parts.length >= 2
      ? (parts[0][0] + parts[1][0]).toUpperCase()
      : parts[0].slice(0, 2).toUpperCase();
  },

  avatarColors: ['avatar-blue', 'avatar-green', 'avatar-purple', 'avatar-amber', 'avatar-rose', 'avatar-cyan'],

  getAvatarColor(name) {
    if (!name) return Utils.avatarColors[0];
    let sum = 0;
    for (let c of name) sum += c.charCodeAt(0);
    return Utils.avatarColors[sum % Utils.avatarColors.length];
  }
};

/* ============================================================
   CUSTOMER LIST PAGE
   ============================================================ */
const CustomerList = {
  init() {
    this.searchInput = document.getElementById('customerSearch');
    this.typeFilter  = document.getElementById('typeFilter');
    this.sourceFilter= document.getElementById('sourceFilter');
    this.statusFilter= document.getElementById('statusFilter');
    this.tableBody   = document.getElementById('customerTableBody');
    this.emptyState  = document.getElementById('emptyState');
    this.resultCount = document.getElementById('resultCount');

    if (!this.searchInput) return;

    this.searchInput.addEventListener('input',
      Utils.debounce(() => this.applyFilters(), 250));

    [this.typeFilter, this.sourceFilter, this.statusFilter].forEach(el => {
      if (el) el.addEventListener('change', () => this.applyFilters());
    });

    this.initDeleteModal();
    this.updateAvatars();
  },

  applyFilters() {
    const query  = (this.searchInput?.value || '').toLowerCase().trim();
    const type   = this.typeFilter?.value || '';
    const source = this.sourceFilter?.value || '';
    const status = this.statusFilter?.value || '';

    const rows = this.tableBody?.querySelectorAll('tr[data-row]') || [];
    let visible = 0;

    rows.forEach(row => {
      const name    = (row.dataset.name    || '').toLowerCase();
      const mobile  = (row.dataset.mobile  || '').toLowerCase();
      const email   = (row.dataset.email   || '').toLowerCase();
      const code    = (row.dataset.code    || '').toLowerCase();
      const rowType = row.dataset.type     || '';
      const rowSrc  = row.dataset.source   || '';
      const rowStat = row.dataset.status   || '';

      const matchSearch = !query ||
        name.includes(query) || mobile.includes(query) ||
        email.includes(query) || code.includes(query);

      const matchType   = !type   || rowType === type;
      const matchSource = !source || rowSrc  === source;
      const matchStatus = !status || rowStat === status;

      const show = matchSearch && matchType && matchSource && matchStatus;
      row.style.display = show ? '' : 'none';
      if (show) visible++;
    });

    if (this.resultCount) {
      this.resultCount.textContent = `${visible} customer${visible !== 1 ? 's' : ''} found`;
    }

    if (this.emptyState) {
      this.emptyState.style.display = visible === 0 ? '' : 'none';
    }
  },

  initDeleteModal() {
    const modal       = document.getElementById('deleteModal');
    const confirmBtn  = document.getElementById('confirmDeleteBtn');
    const customerName= document.getElementById('deleteCustomerName');
    const deleteForm  = document.getElementById('deleteForm');
    let targetId      = null;

    document.querySelectorAll('[data-delete-id]').forEach(btn => {
      btn.addEventListener('click', () => {
        targetId = btn.dataset.deleteId;
        const name = btn.dataset.deleteName || 'this customer';
        if (customerName) customerName.textContent = name;
        if (modal) new bootstrap.Modal(modal).show();
      });
    });

    if (confirmBtn && deleteForm) {
      confirmBtn.addEventListener('click', () => {
        if (!targetId) return;
        Utils.setButtonLoading(confirmBtn, true);
        deleteForm.action = `/customers/${targetId}/delete`;
        deleteForm.submit();
      });
    }
  },

  updateAvatars() {
    document.querySelectorAll('.customer-avatar[data-name]').forEach(el => {
      const name = el.dataset.name || '';
      el.textContent = Utils.getInitials(name);
      el.classList.add(Utils.getAvatarColor(name));
    });
  }
};

/* ============================================================
   ADD / EDIT CUSTOMER FORM
   ============================================================ */
const CustomerForm = {
  _dirty: false,
  _saveNewMode: false,

  init() {
    this.form       = document.getElementById('customerForm');
    this.saveBtn    = document.getElementById('saveBtnMain');
    this.saveNewBtn = document.getElementById('saveNewBtn');
    this.cancelBtn  = document.getElementById('cancelBtn');
    this.unsavedBanner = document.getElementById('unsavedBanner');

    if (!this.form) return;

    this.bindValidation();
    this.bindDirtyTracking();
    this.bindSaveButtons();
    this.bindPhoneFormat();
    this.bindCancelWarning();
  },

  bindValidation() {
    const nameField   = document.getElementById('name');
    const mobileField = document.getElementById('mobile');

    if (nameField) {
      nameField.addEventListener('blur', () => this.validateName(nameField));
      nameField.addEventListener('input', () => {
        if (nameField.classList.contains('is-invalid')) this.validateName(nameField);
      });
    }

    if (mobileField) {
      mobileField.addEventListener('blur', () => this.validateMobile(mobileField));
      mobileField.addEventListener('input', () => {
        if (mobileField.classList.contains('is-invalid')) this.validateMobile(mobileField);
      });
    }
  },

  validateName(field) {
    const val = (field.value || '').trim();
    const err = field.parentElement?.querySelector?.('.field-error-msg') ||
                field.closest('.mb-3')?.querySelector?.('.field-error-msg');

    if (!val || val.length < 2) {
      field.classList.add('is-invalid');
      field.classList.remove('is-valid');
      if (err) err.style.display = 'flex';
      return false;
    }
    field.classList.remove('is-invalid');
    field.classList.add('is-valid');
    if (err) err.style.display = 'none';
    return true;
  },

  validateMobile(field) {
    const val = (field.value || '').trim();
    const err = field.parentElement?.querySelector?.('.field-error-msg') ||
                field.closest('.mb-3')?.querySelector?.('.field-error-msg');
    const ok  = /^[+\d\s\-()]{7,15}$/.test(val);

    if (!val || !ok) {
      field.classList.add('is-invalid');
      field.classList.remove('is-valid');
      if (err) err.style.display = 'flex';
      return false;
    }
    field.classList.remove('is-invalid');
    field.classList.add('is-valid');
    if (err) err.style.display = 'none';
    return true;
  },

  validateAll() {
    const nameField   = document.getElementById('name');
    const mobileField = document.getElementById('mobile');
    const a = nameField   ? this.validateName(nameField)   : true;
    const b = mobileField ? this.validateMobile(mobileField) : true;
    return a && b;
  },

  bindDirtyTracking() {
    const inputs = this.form.querySelectorAll('input, select, textarea');
    inputs.forEach(inp => {
      inp.addEventListener('change', () => {
        this._dirty = true;
        if (this.unsavedBanner) this.unsavedBanner.classList.add('visible');
      });
    });
  },

  bindSaveButtons() {
    if (this.saveBtn) {
      this.saveBtn.addEventListener('click', (e) => {
        e.preventDefault();
        if (!this.validateAll()) return;
        this._saveNewMode = false;
        Utils.setButtonLoading(this.saveBtn, true);
        this._dirty = false;
        this.form.submit();
      });
    }

    if (this.saveNewBtn) {
      this.saveNewBtn.addEventListener('click', (e) => {
        e.preventDefault();
        if (!this.validateAll()) return;
        this._saveNewMode = true;
        Utils.setButtonLoading(this.saveNewBtn, true);
        this._dirty = false;
        const input = document.createElement('input');
        input.type  = 'hidden';
        input.name  = 'saveAndNew';
        input.value = 'true';
        this.form.appendChild(input);
        this.form.submit();
      });
    }
  },

  bindPhoneFormat() {
    const mobileField = document.getElementById('mobile');
    if (!mobileField) return;
    mobileField.addEventListener('input', () => {
      let val = mobileField.value.replace(/[^\d+\s\-()]/g, '');
      mobileField.value = val;
    });
  },

  bindCancelWarning() {
    if (!this.cancelBtn) return;
    this.cancelBtn.addEventListener('click', (e) => {
      if (this._dirty) {
        if (!confirm('You have unsaved changes. Are you sure you want to leave?')) {
          e.preventDefault();
        }
      }
    });
  }
};

/* ============================================================
   CUSTOMER DETAILS PAGE
   ============================================================ */
const CustomerDetail = {
  init() {
    this.initTabs();
    this.initNotes();
    this.updateAvatar();
  },

  initTabs() {
    const tabLinks = document.querySelectorAll('.detail-tab-link');
    const tabPanes = document.querySelectorAll('.detail-tab-pane');

    tabLinks.forEach(link => {
      link.addEventListener('click', (e) => {
        e.preventDefault();
        const target = link.dataset.tab;

        tabLinks.forEach(l => l.classList.remove('active'));
        tabPanes.forEach(p => p.classList.remove('active', 'show'));

        link.classList.add('active');
        const pane = document.getElementById(target);
        if (pane) {
          pane.classList.add('active', 'show');
        }

        // Update URL hash without scroll
        history.replaceState(null, '', `#${target}`);
      });
    });

    // Restore from URL hash
    const hash = window.location.hash?.replace('#', '');
    if (hash) {
      const link = document.querySelector(`[data-tab="${hash}"]`);
      if (link) link.click();
    }
  },

  initNotes() {
    const noteForm    = document.getElementById('noteForm');
    const noteInput   = document.getElementById('noteInput');
    const addNoteBtn  = document.getElementById('addNoteBtn');
    const notesList   = document.getElementById('notesList');
    const notesEmpty  = document.getElementById('notesEmpty');

    if (!addNoteBtn || !noteInput) return;

    addNoteBtn.addEventListener('click', () => {
      const text = (noteInput.value || '').trim();
      if (!text) {
        noteInput.classList.add('is-invalid');
        noteInput.focus();
        return;
      }
      noteInput.classList.remove('is-invalid');

      const now  = new Date();
      const time = now.toLocaleString('en-US', {
        month: 'short', day: 'numeric',
        hour: '2-digit', minute: '2-digit'
      });

      const noteEl = document.createElement('div');
      noteEl.className = 'note-item';
      noteEl.innerHTML = `
        <div class="note-avatar">
          <i class="bi bi-person"></i>
        </div>
        <div class="flex-grow-1">
          <div>
            <span class="note-author">Admin</span>
            <span class="note-time">${time}</span>
          </div>
          <div class="note-text">${text.replace(/</g, '&lt;')}</div>
        </div>`;

      if (notesList) notesList.prepend(noteEl);
      if (notesEmpty) notesEmpty.style.display = 'none';
      noteInput.value = '';

      // Animate in
      noteEl.style.opacity = '0';
      noteEl.style.transform = 'translateY(-8px)';
      requestAnimationFrame(() => {
        noteEl.style.transition = 'all 0.3s ease';
        noteEl.style.opacity    = '1';
        noteEl.style.transform  = 'translateY(0)';
      });
    });

    if (noteInput) {
      noteInput.addEventListener('input', () => {
        noteInput.classList.remove('is-invalid');
      });
    }
  },

  updateAvatar() {
    const el = document.querySelector('.profile-avatar-lg[data-name]');
    if (!el) return;
    const name = el.dataset.name || '';
    el.textContent = Utils.getInitials(name);
  }
};

/* ============================================================
   SIDEBAR / RESPONSIVE
   ============================================================ */
const SidebarManager = {
  init() {
    const toggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    if (toggle && sidebar) {
      toggle.addEventListener('click', () => {
        sidebar.classList.toggle('show');
      });

      document.addEventListener('click', (e) => {
        if (window.innerWidth < 768 &&
            !sidebar.contains(e.target) &&
            !toggle.contains(e.target)) {
          sidebar.classList.remove('show');
        }
      });
    }
  }
};

/* ============================================================
   BOOTSTRAP TOOLTIP INIT
   ============================================================ */
function initTooltips() {
  const tooltipEls = document.querySelectorAll('[data-bs-toggle="tooltip"]');
  tooltipEls.forEach(el => new bootstrap.Tooltip(el, { trigger: 'hover' }));
}

/* ============================================================
   DOM READY
   ============================================================ */
document.addEventListener('DOMContentLoaded', () => {
  CustomerList.init();
  CustomerForm.init();
  CustomerDetail.init();
  SidebarManager.init();
  initTooltips();

  // Auto-dismiss flash alerts after 4s
  document.querySelectorAll('.alert-dismissible').forEach(el => {
    setTimeout(() => {
      const bsAlert = bootstrap.Alert.getOrCreateInstance(el);
      if (bsAlert) bsAlert.close();
    }, 4000);
  });
});