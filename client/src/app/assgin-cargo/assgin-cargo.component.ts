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

  checkVerificationStatus(): void {
    const username = this.authService.getUsername;

    if (!username) {
      Swal.fire('Session Error', 'User not logged in. Please login again.', 'error');
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
        text: 'Your profile is under review by admin.',
        confirmButtonColor: '#3085d6'
      });
    } else if (this.verificationStatus === 'REJECTED') {
      Swal.fire({
        icon: 'error',
        title: 'Profile Rejected',
        text: 'Your profile was rejected. Please update your details.',
        confirmButtonText: 'Update Profile',
        confirmButtonColor: '#d33'
      }).then((result: any) => {
        if (result.isConfirmed) {
          this.router.navigate(['/dashboard/profile']);
        }
      });
    }
  }

  getAssignedCargo(): void {
    this.httpService.getAssignOrders(this.driverId).subscribe({
      next: (data: any[]) => {
        // Filter to show only accepted cargos (hide ASSIGNED/REJECTED from driver)
        this.cargList = data.filter(c => c.status !== 'ASSIGNED' && c.status !== 'REJECTED');
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

  selectCargoForUpdate(cargo: any): void {
    this.selectedCargo = cargo;
  }

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

  // Replace the updateStatus method with this:
updateStatus(cargoId: number, newStatus: string): void {
  // For DELIVERED status, require delivery proof
  if (newStatus === 'DELIVERED' && !this.deliveryProofBase64) {
    Swal.fire({
      icon: 'warning',
      title: 'Delivery Proof Required',
      text: 'Please upload delivery proof before marking as delivered',
      confirmButtonColor: '#d33'
    });
    return;
  }

  // Show OTP input dialog when marking as delivered
  if (newStatus === 'DELIVERED') {
    Swal.fire({
      title: 'Enter Customer OTP',
      html: `
        <p>Please ask the customer for their delivery OTP</p>
        <input type="text" id="otp-input" class="swal2-input" placeholder="Enter 6-digit OTP" maxlength="6">
      `,
      icon: 'info',
      showCancelButton: true,
      confirmButtonText: 'Verify & Deliver',
      cancelButtonText: 'Cancel',
      confirmButtonColor: '#10b981',
      preConfirm: () => {
        const otp = (document.getElementById('otp-input') as HTMLInputElement)?.value;
        if (!otp || otp.length !== 6) {
          Swal.showValidationMessage('Please enter a valid 6-digit OTP');
          return false;
        }
        return otp;
      }
    }).then((result: any) => {
      if (result.isConfirmed && result.value) {
        // First upload the proof, then verify OTP
        this.uploadProofAndVerifyOtp(cargoId, result.value);
      }
    });
    return;
  }

  // For other statuses, proceed directly
  this.proceedWithStatusUpdate(cargoId, newStatus);
}

// Add new method for OTP verification after proof upload
private uploadProofAndVerifyOtp(cargoId: number, otp: string): void {
  const payload: any = {
    deliveryProof: this.deliveryProofBase64
  };

  // First upload proof (status will be AWAITING_OTP)
  this.httpService.updateCargoStatusWithProof(
    cargoId,
    'DELIVERED',
    this.deliveryNotes,
    payload
  ).subscribe({
    next: () => {
      // Now verify OTP
      this.httpService.verifyDriverDeliveryOtp(cargoId, otp).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: 'Delivery Completed!',
            text: 'OTP verified successfully. You are now available for new assignments.',
            timer: 3000,
            showConfirmButton: false
          });
          
          this.selectedCargo = null;
          this.deliveryNotes = '';
          this.deliveryProofBase64 = '';
          this.deliveryProofName = '';
          
          this.getAssignedCargo();
        },
        error: (err) => {
          Swal.fire('Invalid OTP', err.error?.message || 'Please try again', 'error');
        }
      });
    },
    error: (err) => {
      Swal.fire('Error', err.error?.message || 'Failed to upload proof', 'error');
    }
  });
}

  private proceedWithStatusUpdate(cargoId: number, newStatus: string): void {
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
        let message = 'Cargo status updated successfully';
        
        if (newStatus === 'IN_TRANSIT') {
          message = 'Status updated! Customer will receive delivery OTP via email.';
        } else if (newStatus === 'DELIVERED') {
          message = 'Delivery proof uploaded! Awaiting customer OTP verification.';
        }

        Swal.fire({
          icon: 'success',
          title: 'Status Updated!',
          text: message,
          timer: 3000,
          showConfirmButton: false
        });
        
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

  getStatusClass(status: string): string {
    const statusMap: any = {
      'ACCEPTED': 'status-assigned',
      'PICKED_UP': 'status-picked',
      'IN_TRANSIT': 'status-transit',
      'AWAITING_OTP': 'status-awaiting',
      'DELIVERED': 'status-delivered'
    };
    return statusMap[status] || '';
  }

  getNextStatus(currentStatus: string): string[] {
    const transitions: any = {
      'ACCEPTED': ['PICKED_UP'],
      'PICKED_UP': ['IN_TRANSIT'],
      'IN_TRANSIT': ['DELIVERED'],
      'AWAITING_OTP': [] // Waiting for customer OTP verification
    };
    return transitions[currentStatus] || [];
  }
}