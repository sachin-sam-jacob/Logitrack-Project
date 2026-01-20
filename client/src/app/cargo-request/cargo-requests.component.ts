import { Component, OnInit } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';

declare var Swal: any;

@Component({
  selector: 'app-cargo-requests',
  templateUrl: './cargo-requests.component.html',
  styleUrls: ['./cargo-requests.component.scss']
})
export class CargoRequestsComponent implements OnInit {

  pendingRequests: any[] = [];
  driverId!: number;
  
  isLoading: boolean = true;
  isProcessing: boolean = false;
  showError: boolean = false;
  errorMessage: string = '';
  showSuccess: boolean = false;
  successMessage: string = '';
  
  showRejectDialog: boolean = false;
  selectedCargo: any = null;
  rejectionReason: string = '';

  constructor(
    private httpService: HttpService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.driverId = Number(this.authService.getId);
    this.loadPendingRequests();
  }

  loadPendingRequests(): void {
    this.isLoading = true;
    this.httpService.getPendingCargoRequests(this.driverId).subscribe({
      next: (data: any[]) => {
        this.pendingRequests = data;
        this.isLoading = false;
      },
      error: (err:any) => {
        this.showError = true;
        this.errorMessage = err.error?.message || 'Failed to load requests';
        this.isLoading = false;
      }
    });
  }

  acceptCargo(cargoId: number): void {
    Swal.fire({
      title: 'Accept Cargo?',
      text: 'You will be responsible for delivering this cargo',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, Accept',
      confirmButtonColor: '#10b981',
      cancelButtonText: 'Cancel'
    }).then((result: any) => {
      if (result.isConfirmed) {
        this.isProcessing = true;
        this.httpService.acceptCargo(cargoId).subscribe({
          next: (res: any) => {
            Swal.fire('Accepted!', 'Cargo has been accepted', 'success');
            this.loadPendingRequests();
            this.isProcessing = false;
          },
          error: (err:any) => {
            Swal.fire('Error', err.error?.message || 'Failed to accept cargo', 'error');
            this.isProcessing = false;
          }
        });
      }
    });
  }

  openRejectDialog(cargo: any): void {
    this.selectedCargo = cargo;
    this.rejectionReason = '';
    this.showRejectDialog = true;
  }

  closeRejectDialog(): void {
    this.showRejectDialog = false;
    this.selectedCargo = null;
    this.rejectionReason = '';
  }

  confirmReject(): void {
    if (!this.rejectionReason || !this.selectedCargo) {
      return;
    }

    this.isProcessing = true;
    this.httpService.rejectCargo(this.selectedCargo.id, this.rejectionReason).subscribe({
      next: () => {
        Swal.fire('Rejected', 'Cargo has been rejected', 'info');
        this.closeRejectDialog();
        this.loadPendingRequests();
        this.isProcessing = false;
      },
      error: (err:any) => {
        Swal.fire('Error', err.error?.message || 'Failed to reject cargo', 'error');
        this.isProcessing = false;
      }
    });
  }
}