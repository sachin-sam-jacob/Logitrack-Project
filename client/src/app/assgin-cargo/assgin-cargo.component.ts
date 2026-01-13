import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';
 
@Component({
  selector: 'app-assign-cargo',
  templateUrl: './assgin-cargo.component.html'
})
export class AssginCargoComponent implements OnInit {
 
  showError: boolean = false;
  errorMessage: any;
  cargList: any[] = [];
  statusModel: any = {};
  showMessage: any;
  responseMessage: any;
 
  constructor(
    private router: Router,
    private httpService: HttpService,
    private authService: AuthService
  ) {}
 
  ngOnInit(): void {
    this.getAssignCargo();
  }
 
  getAssignCargo() {
    this.httpService.getCargo().subscribe({
      next: (res: any) => this.cargList = res,
      error: (err: any) => {
        this.showError = true;
        this.errorMessage = 'Failed to load assigned cargo';
      }
    });
  }
 
  addStatus(value: any) {
    this.statusModel.status = value;
  }
 
  assignDriver() {
    this.httpService.updateCargoStatus(
      this.statusModel.status,
      this.statusModel.cargoId
    ).subscribe({
      next: (res: any) => {
        this.showMessage = true;
        this.responseMessage = 'Status updated';
      },
      error: (err: any) => {
        this.showError = true;
        this.errorMessage = 'Update failed';
      }
    });
  }
}