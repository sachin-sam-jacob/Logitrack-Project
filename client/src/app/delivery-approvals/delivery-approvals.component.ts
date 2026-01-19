import { Component, OnInit } from '@angular/core';
import { HttpService } from '../../services/http.service';

declare var Swal: any;

@Component({
  selector: 'app-delivery-approvals',
  templateUrl: './delivery-approvals.component.html',
  styleUrls: ['./delivery-approvals.component.scss']
})
export class DeliveryApprovalsComponent implements OnInit {
  pendingCargos: any[] = [];
  selectedCargo: any = null;
  showModal: boolean = false;

  constructor(private httpService: HttpService) {}

  ngOnInit(): void {
    this.loadPendingApprovals();
  }

  loadPendingApprovals(): void {
    this.httpService.getPendingApprovals().subscribe({
      next: (res: any) => {
        this.pendingCargos = res;
      },
      error: () => {
        Swal.fire('Error', 'Failed to load pending approvals', 'error');
      }
    });
  }

  viewProof(cargo: any): void {
    this.selectedCargo = cargo;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedCargo = null;
  }

  approveDelivery(): void {
    if (!this.selectedCargo) return;

    Swal.fire({
      title: 'Approve Delivery?',
      text: 'This will mark the cargo as delivered and update driver location',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, Approve',
      confirmButtonColor: '#10b981',
      cancelButtonText: 'Cancel'
    }).then((result: any) => {
      if (result.isConfirmed) {
        this.httpService.approveDeliveryProof(
          this.selectedCargo.id,
          'APPROVED'
        ).subscribe({
          next: () => {
            Swal.fire({
              icon: 'success',
              title: 'Approved!',
              text: 'Delivery has been approved',
              timer: 2000,
              showConfirmButton: false
            });
            this.closeModal();
            this.loadPendingApprovals();
          },
          error: () => {
            Swal.fire('Error', 'Failed to approve delivery', 'error');
          }
        });
      }
    });
  }

  rejectDelivery(): void {
    if (!this.selectedCargo) return;

    Swal.fire({
      title: 'Reject Delivery Proof?',
      html: `
        <div style="text-align: left; margin-top: 20px;">
          <label style="display: block; margin-bottom: 8px; font-weight: 600;">
            Reason for rejection:
          </label>
          <textarea 
            id="rejection-reason" 
            class="swal2-textarea" 
            placeholder="Enter reason (required)" 
            style="width: 100%; min-height: 100px;"
          ></textarea>
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Reject',
      confirmButtonColor: '#ef4444',
      cancelButtonText: 'Cancel',
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
        this.httpService.approveDeliveryProof(
          this.selectedCargo.id,
          'REJECTED',
          result.value
        ).subscribe({
          next: () => {
            Swal.fire({
              icon: 'info',
              title: 'Rejected',
              text: 'Delivery proof has been rejected',
              timer: 2000,
              showConfirmButton: false
            });
            this.closeModal();
            this.loadPendingApprovals();
          },
          error: () => {
            Swal.fire('Error', 'Failed to reject delivery', 'error');
          }
        });
      }
    });
  }
}