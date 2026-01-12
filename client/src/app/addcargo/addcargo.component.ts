import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';
 
@Component({
  selector: 'app-addcargo',
  templateUrl: './addcargo.component.html'
})
export class AddcargoComponent implements OnInit {
 
  itemForm!: FormGroup;
  formModel: any = { status: null };
  showError: boolean = false;
  errorMessage: any;
  cargList: any[] = [];
  assignModel: any = {};
  driverList: any[] = [];
  showMessage: any;
  responseMessage: any;
 
  constructor(
    private router: Router,
    private httpService: HttpService,
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.itemForm = this.fb.group({
      content: ['', Validators.required],
      size: ['', Validators.required],
      status: ['', Validators.required]
    });
  }
 
  ngOnInit(): void {
    this.getCargo();
    this.getDrivers();
  }
 
  getCargo() {
    this.httpService.getCargo().subscribe({
      next: (res: any) => this.cargList = res,
      error: (err: any) => {
        this.showError = true;
        this.errorMessage = 'Unable to fetch cargo';
      }
    });
  }
 
  getDrivers() {
    this.httpService.getDrivers().subscribe({
      next: (res: any) => this.driverList = res,
      error: (err: any) => {
        this.showError = true;
        this.errorMessage = 'Unable to fetch drivers';
      }
    });
  }
 
  onSubmit() {
    if (this.itemForm.invalid) return;
 
    this.httpService.addCargo(this.itemForm.value).subscribe({
      next: (res: any) => {
        this.showMessage = true;
        this.responseMessage = 'Cargo added successfully';
        this.itemForm.reset();
        this.getCargo();
      },
      error: (err: any) => {
        this.showError = true;
        this.errorMessage = 'Failed to add cargo';
      }
    });
  }
 
  addDriver(value: any) {
    this.assignModel.driverId = value;
  }
 
  assignDriver() {
    this.httpService.assignDriver(
      this.assignModel.driverId,
      this.assignModel.cargoId
    ).subscribe({
      next: (res: any) => {
        this.showMessage = true;
        this.responseMessage = 'Driver assigned successfully';
      },
      error: (err: any) => {
        this.showError = true;
        this.errorMessage = 'Assignment failed';
      }
    });
  }
}