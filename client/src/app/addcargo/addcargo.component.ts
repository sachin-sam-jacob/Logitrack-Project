import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';

declare var Swal: any;

@Component({
  selector: 'app-addcargo',
  templateUrl: './addcargo.component.html',
  styleUrls: ['./addcargo.component.scss']
})
export class AddcargoComponent implements OnInit {

  itemForm!: FormGroup;
  assignForm!: FormGroup;

  cargList: any[] = [];
  driverList: any[] = [];
  filteredDriverList: any[] = [];
  
  selectedCargoId: number | null = null;
  selectedDriverId: number | null = null;
  selectedCargoSourceLocation: string = '';

  showError = false;
  errorMessage = '';
  showMessage = false;
  responseMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private httpService: HttpService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.initForms();
    this.getCargo();
  }

  initForms() {
    this.itemForm = this.fb.group({
      content: ['', Validators.required],
      size: ['', Validators.required],
      sourceLocation: ['', Validators.required],
      destinationLocation: ['', Validators.required],
      customerName: ['', Validators.required],
      customerContact: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      customerAddress: ['', Validators.required],
      estimatedDeliveryDate: ['']
    });
  }

  // Add Cargo
  onSubmit() {
    if (this.itemForm.invalid) {
      this.itemForm.markAllAsTouched();
      Swal.fire('Incomplete Form', 'Please fill all required fields', 'warning');
      return;
    }

    const payload = {
      ...this.itemForm.value,
      estimatedDeliveryDate: this.itemForm.value.estimatedDeliveryDate 
        ? new Date(this.itemForm.value.estimatedDeliveryDate).toISOString()
        : null
    };

    this.httpService.addCargo(payload).subscribe({
      next: (res: any) => {
        Swal.fire({
          icon: 'success',
          title: 'Success!',
          text: 'Cargo created successfully',
          timer: 2000,
          showConfirmButton: false
        });
        this.itemForm.reset();
        this.getCargo();
      },
      error: (err) => {
        Swal.fire('Error', err.error?.message || 'Failed to add cargo', 'error');
      }
    });
  }

  // Fetch Cargos
  getCargo() {
    this.httpService.getCargo().subscribe({
      next: (res: any) => {
        this.cargList = res;
      },
      error: () => {
        Swal.fire('Error', 'Unable to fetch cargo', 'error');
      }
    });
  }

  // Select cargo and load drivers for its source location
  selectCargo(event: Event) {
    const target = event.target as HTMLSelectElement;
    const cargoId = Number(target.value);
    
    if (!cargoId) {
      this.selectedCargoId = null;
      this.filteredDriverList = [];
      return;
    }

    this.selectedCargoId = cargoId;
    const cargo = this.cargList.find(c => c.id === cargoId);
    
    if (cargo) {
      this.selectedCargoSourceLocation = cargo.sourceLocation;
      this.loadDriversForLocation(cargo.sourceLocation);
    }
  }

  // Load drivers available at source location
  loadDriversForLocation(sourceLocation: string) {
    this.httpService.getDriversForLocation(sourceLocation).subscribe({
      next: (res: any) => {
        this.filteredDriverList = res;
        
        if (this.filteredDriverList.length === 0) {
          Swal.fire({
            icon: 'info',
            title: 'No Drivers Available',
            text: `No approved drivers available at ${sourceLocation}`,
            confirmButtonColor: '#3085d6'
          });
        }
      },
      error: () => {
        Swal.fire('Error', 'Unable to fetch drivers', 'error');
      }
    });
  }

  // Select driver
  selectDriver(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.selectedDriverId = Number(target.value);
  }

  // Assign driver to cargo
  assignDriver() {
    if (!this.selectedCargoId || !this.selectedDriverId) {
      Swal.fire('Error', 'Please select both cargo and driver', 'warning');
      return;
    }

    this.httpService.assignDriver(this.selectedDriverId, this.selectedCargoId).subscribe({
      next: (res: any) => {
        Swal.fire({
          icon: 'success',
          title: 'Assigned!',
          text: res.message || 'Driver assigned successfully',
          timer: 2000,
          showConfirmButton: false
        });
        this.getCargo();
        this.selectedCargoId = null;
        this.selectedDriverId = null;
        this.filteredDriverList = [];
      },
      error: (err) => {
        Swal.fire('Error', err.error?.message || 'Assignment failed', 'error');
      }
    });
  }
}