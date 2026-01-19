// admin-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';

declare var Swal: any;

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  
  activeTab: string = 'statistics';
  pendingDrivers: any[] = []; // Initialize as empty array
  allDrivers: any[] = []; // Initialize as empty array
  statistics: any = {
    totalDrivers: 0,
    pendingDrivers: 0,
    approvedDrivers: 0,
    rejectedDrivers: 0
  };
  isLoading: boolean = true;

  selectedDriver: any = null;
  showDocumentModal: boolean = false;

  constructor(
    private httpService: HttpService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStatistics();
    this.loadPendingDrivers();
  }

  switchTab(tab: string) {
    this.activeTab = tab;
    if (tab === 'pending') {
      this.loadPendingDrivers();
    } else if (tab === 'all') {
      this.loadAllDrivers();
    } else if (tab === 'statistics') {
      this.loadStatistics();
    }
  }

  loadStatistics() {
    this.httpService.getAdminStatistics().subscribe({
      next: (data) => {
        this.statistics = data;
      },
      error: (err) => {
        console.error('Failed to load statistics', err);
        Swal.fire('Error', 'Failed to load statistics', 'error');
      }
    });
  }

  loadPendingDrivers() {
    this.isLoading = true;
    this.httpService.getPendingDrivers().subscribe({
      next: (data) => {
        this.pendingDrivers = data;
        this.isLoading = false;
      },
      error: (err) => {
        Swal.fire('Error', 'Failed to load pending drivers', 'error');
        this.isLoading = false;
      }
    });
  }

  loadAllDrivers() {
    this.isLoading = true;
    this.httpService.getAllDriversAdmin().subscribe({
      next: (data) => {
        this.allDrivers = data;
        this.isLoading = false;
      },
      error: (err) => {
        Swal.fire('Error', 'Failed to load drivers', 'error');
        this.isLoading = false;
      }
    });
  }

  viewDocuments(driver: any) {
    this.selectedDriver = driver;
    this.showDocumentModal = true;
  }

  closeModal() {
    this.showDocumentModal = false;
    this.selectedDriver = null;
  }

  // NEW: Handle image loading errors
  handleImageError(event: any, type: string) {
    console.error(`Failed to load ${type} image`);
    event.target.src = 'assets/placeholder-document.png'; // Optional: add a placeholder image
    event.target.alt = `Unable to load ${type} document`;
    
    // Show error message
    Swal.fire({
      icon: 'warning',
      title: 'Document Load Error',
      text: `Unable to load ${type} document. The file may be corrupted or unavailable.`,
      confirmButtonColor: '#3085d6'
    });
  }

  approveDriver(driverId: number) {
    Swal.fire({
      title: 'Approve Driver?',
      text: 'This driver will be able to receive cargo assignments',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, Approve',
      confirmButtonColor: '#10b981',
      cancelButtonText: 'Cancel',
      cancelButtonColor: '#64748b'
    }).then((result: any) => {
      if (result.isConfirmed) {
        this.httpService.verifyDriver(driverId, 'APPROVED').subscribe({
          next: () => {
            Swal.fire({
              icon: 'success',
              title: 'Approved!',
              text: 'Driver has been approved successfully',
              timer: 2000,
              showConfirmButton: false
            });
            
            // Refresh data
            this.loadPendingDrivers();
            this.loadStatistics();
            
            // Close modal if open
            if (this.showDocumentModal) {
              this.closeModal();
            }
          },
          error: () => {
            Swal.fire('Error', 'Failed to approve driver', 'error');
          }
        });
      }
    });
  }

  rejectDriver(driverId: number) {
    Swal.fire({
      title: 'Reject Driver?',
      html: `
        <div style="text-align: left; margin-top: 20px;">
          <label style="display: block; margin-bottom: 8px; font-weight: 600; color: #475569;">
            Reason for rejection:
          </label>
          <textarea 
            id="rejection-reason" 
            class="swal2-textarea" 
            placeholder="Enter reason (required)" 
            style="width: 100%; min-height: 100px; padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; font-family: inherit;"
          ></textarea>
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Reject',
      confirmButtonColor: '#ef4444',
      cancelButtonText: 'Cancel',
      cancelButtonColor: '#64748b',
      preConfirm: () => {
        const reason = (document.getElementById('rejection-reason') as HTMLTextAreaElement)?.value;
        if (!reason || reason.trim() === '') {
          Swal.showValidationMessage('Please provide a reason for rejection');
          return false;
        }
        return reason;
      }
    }).then((result: any) => {
      if (result.isConfirmed && result.value) {
        this.httpService.verifyDriver(driverId, 'REJECTED', result.value).subscribe({
          next: () => {
            Swal.fire({
              icon: 'info',
              title: 'Rejected',
              text: 'Driver has been rejected',
              timer: 2000,
              showConfirmButton: false
            });
            
            // Refresh data
            this.loadPendingDrivers();
            this.loadStatistics();
            
            // Close modal if open
            if (this.showDocumentModal) {
              this.closeModal();
            }
          },
          error: () => {
            Swal.fire('Error', 'Failed to reject driver', 'error');
          }
        });
      }
    });
  }

  // NEW: Logout functionality
  logout() {
    Swal.fire({
      title: 'Logout Confirmation',
      text: 'Are you sure you want to logout?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, Logout',
      cancelButtonText: 'Cancel',
      confirmButtonColor: '#ef4444',
      cancelButtonColor: '#64748b',
      reverseButtons: true
    }).then((result: any) => {
      if (result.isConfirmed) {
        // Clear authentication
        this.authService.logout();

        // Show success message
        Swal.fire({
          icon: 'success',
          title: 'Logged Out',
          text: 'You have been logged out successfully',
          timer: 1500,
          showConfirmButton: false
        });

        // Redirect to login
        setTimeout(() => {
          this.router.navigateByUrl('/login');
        }, 1500);
      }
    });
  }
}