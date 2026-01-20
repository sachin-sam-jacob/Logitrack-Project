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

  cargList: any[] = [];
  driverList: any[] = [];
  filteredDriverList: any[] = [];
  
  assignModel: any = {
    cargoId: null,
    driverId: null
  };

  selectedCargoSourceLocation: string = '';
  isSubmitting = false;
  isAssigning = false;

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
    this.initForm();
    this.getCargo();
    this.getDrivers();
  }

  initForm() {
    this.itemForm = this.fb.group({
      content: ['', Validators.required],
      size: ['', Validators.required],
      sourceLocation: ['', Validators.required],
      destinationLocation: ['', Validators.required],
      customerName: ['', Validators.required],
      customerContact: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      customerEmail: ['', [Validators.required, Validators.email]],
      customerAddress: ['', Validators.required]
    });
  }

  onSourceLocationChange() {
    const sourceLocation = this.itemForm.get('sourceLocation')?.value;
    console.log('Source location changed to:', sourceLocation);
  }

  onSubmit() {
    if (this.itemForm.invalid) {
      this.itemForm.markAllAsTouched();
      Swal.fire({
        icon: 'warning',
        title: 'Incomplete Form',
        text: 'Please fill all required fields correctly',
        confirmButtonColor: '#3b82f6'
      });
      return;
    }

    this.isSubmitting = true;
    this.showError = false;
    this.showMessage = false;

    console.log('Submitting cargo with data:', this.itemForm.value);

    this.httpService.addCargo(this.itemForm.value).subscribe({
      next: (res: any) => {
        this.isSubmitting = false;
        
        Swal.fire({
          icon: 'success',
          title: 'Cargo Created!',
          html: `
            <p>Cargo created successfully!</p>
            <p><strong>Tracking Number:</strong> ${res.trackingNumber || 'Generating...'}</p>
            <p>Tracking details sent to: <strong>${this.itemForm.value.customerEmail}</strong></p>
          `,
          confirmButtonColor: '#10b981'
        });

        this.itemForm.reset();
        this.getCargo();
      },
      error: (err) => {
        this.isSubmitting = false;
        this.showError = true;
        this.errorMessage = err.error?.message || 'Failed to add cargo';
        
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: this.errorMessage,
          confirmButtonColor: '#ef4444'
        });
      }
    });
  }

  getCargo() {
    this.httpService.getCargo().subscribe({
      next: (res: any) => {
        this.cargList = res;
        console.log('Loaded cargos:', this.cargList);
      },
      error: () => {
        this.showError = true;
        this.errorMessage = 'Unable to fetch cargo';
      }
    });
  }

  getDrivers() {
    this.httpService.getAllDrivers().subscribe({
      next: (res: any) => {
        this.driverList = res;
        console.log('Loaded drivers:', this.driverList);
      },
      error: () => {
        this.showError = true;
        this.errorMessage = 'Unable to fetch drivers';
      }
    });
  }

  selectCargo(event: Event) {
    const target = event.target as HTMLSelectElement;
    const cargoId = target.value;
    
    this.assignModel.cargoId = cargoId;
    this.assignModel.driverId = null;

    if (cargoId) {
      const selectedCargo = this.cargList.find(c => c.id == cargoId);
      if (selectedCargo) {
        this.selectedCargoSourceLocation = selectedCargo.sourceLocation;
        console.log('Selected cargo source location:', this.selectedCargoSourceLocation);
        this.filterDriversByLocation(this.selectedCargoSourceLocation);
      }
    } else {
      this.selectedCargoSourceLocation = '';
      this.filteredDriverList = [];
    }
  }

  filterDriversByLocation(sourceLocation: string) {
    if (!sourceLocation) {
      this.filteredDriverList = [];
      return;
    }

    this.filteredDriverList = this.driverList.filter(driver => {
      const driverLocation = (driver.currentLocation || driver.baseLocation || '').trim().toLowerCase();
      const cargoLocation = sourceLocation.trim().toLowerCase();
      
      console.log(`Comparing driver location: "${driverLocation}" with cargo location: "${cargoLocation}"`);
      
      return driverLocation === cargoLocation;
    });

    console.log('Filtered drivers:', this.filteredDriverList);

    if (this.filteredDriverList.length === 0) {
      Swal.fire({
        icon: 'info',
        title: 'No Drivers Available',
        text: `No drivers found at location: ${sourceLocation}`,
        confirmButtonColor: '#3b82f6'
      });
    }
  }

  selectDriver(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.assignModel.driverId = target.value;
    console.log('Selected driver ID:', this.assignModel.driverId);
  }

  assignDriver() {
    if (!this.assignModel.cargoId || !this.assignModel.driverId) {
      Swal.fire({
        icon: 'warning',
        title: 'Missing Selection',
        text: 'Please select both cargo and driver',
        confirmButtonColor: '#f59e0b'
      });
      return;
    }

    this.isAssigning = true;
    this.showError = false;
    this.showMessage = false;

    this.httpService.assignDriver(
      this.assignModel.driverId,
      this.assignModel.cargoId
    ).subscribe({
      next: (res: any) => {
        this.isAssigning = false;
        
        Swal.fire({
          icon: 'success',
          title: 'Assigned!',
          text: res.message || 'Driver assigned successfully',
          confirmButtonColor: '#10b981'
        });

        this.assignModel = { cargoId: null, driverId: null };
        this.selectedCargoSourceLocation = '';
        this.filteredDriverList = [];
        this.getCargo();
      },
      error: (err) => {
        this.isAssigning = false;
        
        Swal.fire({
          icon: 'error',
          title: 'Assignment Failed',
          text: err.error?.message || 'Failed to assign driver',
          confirmButtonColor: '#ef4444'
        });
      }
    });
  }
}
