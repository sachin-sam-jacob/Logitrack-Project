import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-addcargo',
  templateUrl: './addcargo.component.html',
  styleUrls: ['./addcargo.component.scss']
})
export class AddcargoComponent implements OnInit {

  itemForm!: FormGroup;

  cargList: any[] = [];
  driverList: any[] = [];
  assignModel: any = {
    cargoId: null,
    driverId: null
  };

  showError = false;
  errorMessage = '';
  showMessage = false;
  responseMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private httpService: HttpService,
    private authService:AuthService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.getCargo();
    this.getDrivers();
  }

  initForm() {
    this.itemForm = this.fb.group({
      content: ['', Validators.required],
      size: ['', Validators.required],
      status: ['', Validators.required]
    });
  }

  // ---------------- ADD CARGO ----------------
  onSubmit() {
    if (this.itemForm.invalid) {
      this.itemForm.markAllAsTouched();
      return;
    }

    this.httpService.addCargo(this.itemForm.value).subscribe({
      next: (res:any) => {
        this.showMessage=true;
        this.responseMessage="Cargo Created Successfully!"
        this.itemForm.reset();
        this.getCargo();
      },
      error: () => {
        this.showError = true;
        this.errorMessage = 'Failed to add cargo';
      }
    });
  }

  // ---------------- FETCH DATA ----------------
  getCargo() {
    this.httpService.getCargo().subscribe({
      next: (res: any) => this.cargList = res,
      error: () => {
        this.showError = true;
        this.errorMessage = 'Unable to fetch cargo';
      }
    });
  }

  getDrivers() {
    this.httpService.getDrivers().subscribe({
      next: (res: any) => this.driverList = res,
      error: () => {
        this.showError = true;
        this.errorMessage = 'Unable to fetch drivers';
      }
    });
  }

  // ---------------- ASSIGN DRIVER ----------------
  selectCargo(event: Event) {
    const tar=event.target as HTMLSelectElement;
    this.assignModel.cargoId =tar.value;
  }

  selectDriver(event: Event) {
  const target = event.target as HTMLSelectElement; // Type assertion
  this.assignModel.driverId = target.value;
}

  assignDriver() {
    if (!this.assignModel.cargoId || !this.assignModel.driverId) {
      this.showError = true;
      this.errorMessage = 'Please select both cargo and driver';
      return;
    }

    this.httpService.assignDriver(
      this.assignModel.driverId,
      this.assignModel.cargoId
    ).subscribe({
      next: (res: any) => {
        this.showMessage = true;
        this.responseMessage = res.message;
        this.getCargo();
      },
      error: () => {
        this.showError = true;
        this.errorMessage = 'Assignment failed';
      }
    });
  }
}
