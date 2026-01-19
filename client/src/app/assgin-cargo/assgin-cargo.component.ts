import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';

declare var Swal: any;

@Component({
  selector: 'app-assign-cargo',
  templateUrl: './assgin-cargo.component.html',
  styleUrls: ['./assgin-cargo.component.scss']
})
export class AssginCargoComponent implements OnInit {

  cargList: any[] = [];
  driverId!: number | null;
  isApproved: boolean = false;
  verificationStatus: string = '';

  selectedCargo: any = null;
  deliveryNotes: string = '';
  deliveryProofBase64: string = '';
  deliveryProofName: string = '';

  showError = false;
  errorMessage = '';
  showMessage = false;
  responseMessage = '';

  constructor(
    public router: Router,
    private httpService: HttpService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.driverId = Number(this.authService.getId);
    this.checkVerificationStatus();
  }

  // Check if driver is approved
  checkVerificationStatus(): void {
  const username = this.authService.getUsername;

  if (!username) {
    Swal.fire(
      'Session Error',
      'User not logged in. Please login again.',
      'error'
    );
    this.router.navigate(['/login']);
    return;
  }

  this.httpService.getDriverVerificationStatus(username).subscribe({
    next: (res: any) => {
      this.verificationStatus = res.verificationStatus;
      this.isApproved = this.verificationStatus === 'APPROVED';

      if (this.isApproved) {
        this.getAssignedCargo();
      } else {
        this.showStatusMessage();
      }
    },
    error: () => {
      Swal.fire('Error', 'Failed to check verification status', 'error');
    }
  });
}


  showStatusMessage(): void {
    if (this.verificationStatus === 'PENDING') {
      Swal.fire({
        icon: 'info',
        title: 'Verification Pending',
        text: 'Your profile is under review by admin. You will be able to access features once approved.',
        confirmButtonColor: '#3085d6'
      });
    } else if (this.verificationStatus === 'REJECTED') {
      Swal.fire({
        icon: 'error',
        title: 'Profile Rejected',
        text: 'Your profile was rejected. Please update your details and resubmit.',
        confirmButtonText: 'Update Profile',
        confirmButtonColor: '#d33'
      }).then((result: any) => {
        if (result.isConfirmed) {
          this.router.navigate(['/dashboard/profile']);
        }
      });
    }
  }

  // Get assigned cargos
  getAssignedCargo(): void {
    this.httpService.getAssignOrders(this.driverId).subscribe({
      next: (data: any[]) => {
        this.cargList = data;
      },
      error: (err) => {
        if (err.status === 403) {
          Swal.fire({
            icon: 'warning',
            title: 'Access Denied',
            text: err.error?.message || 'You need admin approval to access cargos',
            confirmButtonColor: '#d33'
          });
        } else {
          this.showError = true;
          this.errorMessage = 'Failed to load assigned cargos';
        }
      }
    });
  }

  // Select cargo for status update
  selectCargoForUpdate(cargo: any): void {
    this.selectedCargo = cargo;
  }

  // Handle delivery proof file upload
  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        Swal.fire('File Too Large', 'Please upload a file smaller than 5MB', 'warning');
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        this.deliveryProofBase64 = reader.result as string;
        this.deliveryProofName = file.name;
      };
      reader.readAsDataURL(file);
    }
  }

  // Update cargo status
  updateStatus(cargoId: number, newStatus: string): void {
    // If status is DELIVERED, require delivery proof
    if (newStatus === 'DELIVERED' && !this.deliveryProofBase64) {
      Swal.fire({
        icon: 'warning',
        title: 'Delivery Proof Required',
        text: 'Please upload delivery proof before marking as delivered',
        confirmButtonColor: '#d33'
      });
      return;
    }

    const payload: any = {
      deliveryProof: this.deliveryProofBase64 || null
    };

    this.httpService.updateCargoStatusWithProof(
      cargoId,
      newStatus,
      this.deliveryNotes,
      payload
    ).subscribe({
      next: (res: any) => {
        Swal.fire({
          icon: 'success',
          title: 'Status Updated!',
          text: res.message || 'Cargo status updated successfully',
          timer: 2000,
          showConfirmButton: false
        });
        
        // Reset form
        this.selectedCargo = null;
        this.deliveryNotes = '';
        this.deliveryProofBase64 = '';
        this.deliveryProofName = '';
        
        this.getAssignedCargo();
      },
      error: (err) => {
        Swal.fire('Error', err.error?.message || 'Failed to update status', 'error');
      }
    });
  }

  // Get status badge class
  getStatusClass(status: string): string {
    const statusMap: any = {
      'ASSIGNED': 'status-assigned',
      'PICKED_UP': 'status-picked',
      'IN_TRANSIT': 'status-transit',
      'AWAITING_APPROVAL': 'status-awaiting',
      'DELIVERED': 'status-delivered'
    };
    return statusMap[status] || '';
  }

  // Get next available status
  getNextStatus(currentStatus: string): string[] {
    const transitions: any = {
      'ASSIGNED': ['PICKED_UP'],
      'PICKED_UP': ['IN_TRANSIT'],
      'IN_TRANSIT': ['DELIVERED'],
      'AWAITING_APPROVAL': [] // No action needed, awaiting business approval
    };
    return transitions[currentStatus] || [];
  }
}