import { Component } from '@angular/core';
import { HttpService } from '../../services/http.service';

declare var Swal: any;

@Component({
  selector: 'app-customer-track-cargo',
  templateUrl: './customer-track-cargo.component.html',
  styleUrls: ['./customer-track-cargo.component.scss']
})
export class CustomerTrackCargoComponent {

  searchMode: string = 'email';
  customerEmail: string = '';
  trackingNumber: string = '';
  
  cargos: any[] = [];
  singleCargo: any = null;
  
  showError: boolean = false;
  errorMessage: string = '';
  hasSearched: boolean = false;

  // OTP verification
  showOtpModal: boolean = false;
  otpInput: string = '';
  selectedCargoForOtp: any = null;

  constructor(private httpService: HttpService) {}

  switchSearchMode(mode: string): void {
    this.searchMode = mode;
    this.resetSearch();
  }

  resetSearch(): void {
    this.cargos = [];
    this.singleCargo = null;
    this.showError = false;
    this.errorMessage = '';
    this.hasSearched = false;
  }

  searchByEmail(): void {
    if (!this.customerEmail) {
      this.showError = true;
      this.errorMessage = 'Please enter your email address';
      return;
    }

    this.hasSearched = true;
    this.httpService.getCargosByEmail(this.customerEmail).subscribe({
      next: (data: any[]) => {
        this.cargos = data;
        this.showError = false;
      },
      error: () => {
        this.showError = true;
        this.errorMessage = 'Failed to load cargos';
      }
    });
  }

  searchByTracking(): void {
    if (!this.trackingNumber) {
      this.showError = true;
      this.errorMessage = 'Please enter tracking number';
      return;
    }

    this.hasSearched = true;
    this.httpService.trackByTrackingNumber(this.trackingNumber).subscribe({
      next: (data: any) => {
        this.singleCargo = data;
        this.showError = false;
      },
      error: () => {
        this.showError = true;
        this.errorMessage = 'Cargo not found';
      }
    });
  }

  getStatusClass(status: string): string {
    const normalizedStatus = status?.toLowerCase().replace(/_/g, '_') || '';
    
    // Map customer-visible statuses
    if (normalizedStatus === 'assigned' || normalizedStatus === 'accepted' || normalizedStatus === 'rejected') {
      return 'created'; // Show as "CREATED" for customer
    }
    
    return normalizedStatus;
  }

  getCustomerVisibleStatus(status: string): string {
    // Hide internal statuses from customer
    if (status === 'ASSIGNED' || status === 'ACCEPTED' || status === 'REJECTED') {
      return 'CREATED';
    }
    if (status === 'AWAITING_OTP') {
      return 'OUT FOR DELIVERY';
    }
    return status;
  }

  isStepCompleted(currentStatus: string, checkStatus: string): boolean {
    const statusOrder = ['CREATED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED'];
    
    // Normalize current status for customer view
    let normalizedCurrent = currentStatus;
    if (currentStatus === 'ASSIGNED' || currentStatus === 'ACCEPTED' || currentStatus === 'REJECTED') {
      normalizedCurrent = 'CREATED';
    }
    if (currentStatus === 'AWAITING_OTP') {
      normalizedCurrent = 'IN_TRANSIT';
    }
    
    const currentIndex = statusOrder.indexOf(normalizedCurrent);
    const checkIndex = statusOrder.indexOf(checkStatus);
    return currentIndex >= checkIndex;
  }

  // NEW: Check if cargo needs OTP verification
  needsOtpVerification(cargo: any): boolean {
    return cargo.status === 'AWAITING_OTP' && !cargo.otpVerified;
  }

  // NEW: Open OTP modal
  openOtpModal(cargo: any): void {
    this.selectedCargoForOtp = cargo;
    this.otpInput = '';
    this.showOtpModal = true;
  }

  // NEW: Close OTP modal
  closeOtpModal(): void {
    this.showOtpModal = false;
    this.selectedCargoForOtp = null;
    this.otpInput = '';
  }

  // NEW: Verify OTP
  verifyOtp(): void {
    if (!this.otpInput || this.otpInput.length !== 6) {
      Swal.fire('Invalid OTP', 'Please enter a 6-digit OTP', 'warning');
      return;
    }

    this.httpService.verifyDeliveryOtp(this.selectedCargoForOtp.id, this.otpInput).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Delivery Verified!',
          text: 'Thank you for confirming the delivery',
          timer: 2000,
          showConfirmButton: false
        });
        
        this.closeOtpModal();
        
        // Refresh cargo data
        if (this.searchMode === 'email') {
          this.searchByEmail();
        } else {
          this.searchByTracking();
        }
      },
      error: (err:any) => {
        Swal.fire('Verification Failed', err.error?.message || 'Invalid OTP', 'error');
      }
    });
  }
}