import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-assign-cargo',
  templateUrl: './assgin-cargo.component.html',
  styleUrls: ['./assgin-cargo.component.scss']
})
export class AssginCargoComponent implements OnInit {

  cargList: any[] = [];

  statusModel = {
    cargoId: null as number | null,
    newStatus: null as string | null
  };

  showError = false;
  errorMessage = '';
  showMessage = false;
  responseMessage = '';

  driverId!: number | null;

  constructor(
    public router: Router,
    private httpService: HttpService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.driverId = Number(this.authService.getId); // Logged-in DRIVER id\
    //console.log("Driver id received",this.driverId)
    this.getAssignedCargo();
  }

  // ✅ Get cargos assigned to this driver
  getAssignedCargo(): void {
    this.httpService.getAssignOrders(this.driverId).subscribe(
      (data: any[]) => {
        this.cargList = data;
      },
      () => {
        this.showError = true;
        this.errorMessage = 'Failed to load assigned cargos';
      }
    );
  }

  // ✅ Capture selected status
  onStatusChange(status: string, cargoId: number): void {
    this.statusModel.newStatus = status;
    this.statusModel.cargoId = cargoId;
  }

  // ✅ Update cargo status
  updateStatus(): void {
    if (!this.statusModel.newStatus || !this.statusModel.cargoId) {
      this.showError = true;
      this.errorMessage = 'Please select a status before updating';
      return;
    }

    this.showError = false;
    this.showMessage = false;

    this.httpService
      .updateCargoStatus(this.statusModel.newStatus, this.statusModel.cargoId)
      .subscribe(
        (res: any) => {
          this.showMessage = true;
          this.responseMessage = res.message || 'Status updated successfully';
          this.getAssignedCargo();
        },
        () => {
          this.showError = true;
          this.errorMessage = 'Error updating cargo status';
        }
      );
  }
}
