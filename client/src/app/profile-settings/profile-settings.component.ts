import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';

declare var Swal: any;

@Component({
  selector: 'app-profile-settings',
  templateUrl: './profile-settings.component.html',
  styleUrls: ['./profile-settings.component.scss']
})
export class ProfileSettingsComponent implements OnInit {
  detailsForm!: FormGroup;
  role: string = '';
  username: string = '';
  isSubmitting = false;
  detailsCompleted = false;
  isLoading = true;

  // File uploads
  licenseProofBase64: string = '';
  vehicleRcBase64: string = '';
  licenseProofName: string = '';
  vehicleRcName: string = '';

  // Driver status - ADDED missing fields
  verificationStatus: string = '';
  rejectionReason: string = '';
  isAvailable: boolean = false;
  baseLocation: string = '';
  currentLocation: string = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private httpService: HttpService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.username = this.authService.getUsername || '';
    this.role = this.authService.getRole || '';

    if (!this.username || !this.role) {
      this.router.navigate(['/login']);
      return;
    }

    this.initForm();
    this.loadExistingDetails();
  }

  initForm() {
    if (this.role === 'BUSINESS') {
      this.detailsForm = this.fb.group({
        businessName: ['', Validators.required],
        location: ['', Validators.required],
        contactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
        businessType: ['', Validators.required]
      });
    } else if (this.role === 'DRIVER') {
      this.detailsForm = this.fb.group({
        licenseNumber: ['', Validators.required],
        vehicleType: ['', Validators.required],
        vehicleNumber: ['', Validators.required],
        contactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
        location: ['', Validators.required]
      });
    } else if (this.role === 'CUSTOMER') {
      this.detailsForm = this.fb.group({
        contactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
        alternativeContactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
        address: ['', Validators.required],
        location: ['', Validators.required]
      });
    }
  }

  loadExistingDetails() {
    let getDetailsObservable;
    
    if (this.role === 'BUSINESS') {
      getDetailsObservable = this.httpService.getBusinessDetails(this.username);
    } else if (this.role === 'DRIVER') {
      getDetailsObservable = this.httpService.getDriverDetails(this.username);
    } else {
      getDetailsObservable = this.httpService.getCustomerDetails(this.username);
    }

    getDetailsObservable.subscribe({
      next: (data: any) => {
        this.detailsCompleted = data.detailsCompleted || false;
        
        if (this.role === 'BUSINESS') {
          this.detailsForm.patchValue({
            businessName: data.businessName || '',
            location: data.location || '',
            contactNumber: data.contactNumber || '',
            businessType: data.businessType || ''
          });
        } else if (this.role === 'DRIVER') {
          this.detailsForm.patchValue({
            licenseNumber: data.licenseNumber || '',
            vehicleType: data.vehicleType || '',
            vehicleNumber: data.vehicleNumber || '',
            contactNumber: data.contactNumber || '',
            location: data.baseLocation || data.location || ''
          });
          
          // FIXED: Properly set all driver fields
          this.verificationStatus = data.verificationStatus || 'PENDING';
          this.rejectionReason = data.rejectionReason || '';
          this.isAvailable = data.available || false;
          this.baseLocation = data.baseLocation || '';
          this.currentLocation = data.currentLocation || data.baseLocation || '';
          
          console.log('Driver details loaded:', {
            verificationStatus: this.verificationStatus,
            isAvailable: this.isAvailable,
            baseLocation: this.baseLocation,
            currentLocation: this.currentLocation
          });
        } else if (this.role === 'CUSTOMER') {
          this.detailsForm.patchValue({
            contactNumber: data.contactNumber || '',
            alternativeContactNumber: data.alternativeContactNumber || '',
            address: data.address || '',
            location: data.location || ''
          });
        }
        
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load details:', err);
        this.isLoading = false;
      }
    });
  }

  onFileChange(event: any, type: string) {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        Swal.fire('File Too Large', 'Please upload a file smaller than 5MB', 'warning');
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        const base64 = reader.result as string;
        if (type === 'license') {
          this.licenseProofBase64 = base64;
          this.licenseProofName = file.name;
        } else if (type === 'rc') {
          this.vehicleRcBase64 = base64;
          this.vehicleRcName = file.name;
        }
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit() {
    if (this.detailsForm.invalid) {
      this.detailsForm.markAllAsTouched();
      
      const errors: string[] = [];
      Object.keys(this.detailsForm.controls).forEach(key => {
        const control = this.detailsForm.get(key);
        if (control?.invalid) {
          if (control.errors?.['required']) {
            errors.push(`${this.formatFieldName(key)} is required`);
          }
          if (control.errors?.['pattern']) {
            errors.push(`${this.formatFieldName(key)} is invalid`);
          }
        }
      });

      Swal.fire({
        icon: 'warning',
        title: 'Incomplete Form',
        html: errors.join('<br>'),
        confirmButtonColor: '#3085d6'
      });
      return;
    }

    this.isSubmitting = true;

    const payload = {
      username: this.username,
      ...this.detailsForm.value
    };

    if (this.role === 'DRIVER') {
      if (this.licenseProofBase64) {
        payload['licenseProof'] = this.licenseProofBase64;
      }
      if (this.vehicleRcBase64) {
        payload['vehicleRc'] = this.vehicleRcBase64;
      }
    }

    let submitObservable;
    if (this.role === 'BUSINESS') {
      submitObservable = this.httpService.submitBusinessDetails(payload);
    } else if (this.role === 'DRIVER') {
      submitObservable = this.httpService.submitDriverDetails(payload);
    } else {
      submitObservable = this.httpService.submitCustomerDetails(payload);
    }

    submitObservable.subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Profile Updated!',
          text: 'Your details have been saved successfully',
          timer: 2000,
          showConfirmButton: false
        }).then(() => {
          this.detailsCompleted = true;
          if (this.role === 'DRIVER') {
            this.verificationStatus = 'PENDING';
          }
          this.loadExistingDetails();
        });
        this.isSubmitting = false;
      },
      error: (err) => {
        console.error('Update error:', err);
        Swal.fire({
          icon: 'error',
          title: 'Update Failed',
          text: err.error?.message || 'Please try again',
          confirmButtonColor: '#d33'
        });
        this.isSubmitting = false;
      }
    });
  }

  toggleAvailability() {
    if (this.role !== 'DRIVER') {
      console.error('Toggle availability called for non-driver role');
      return;
    }

    if (this.verificationStatus !== 'APPROVED') {
      Swal.fire({
        icon: 'warning',
        title: 'Not Approved',
        text: 'Only approved drivers can change availability',
        confirmButtonColor: '#f59e0b'
      });
      return;
    }

    console.log('Toggling availability for:', this.username);

    this.httpService.toggleDriverAvailability(this.username).subscribe({
      next: (res: any) => {
        this.isAvailable = res.isAvailable;
        console.log('Availability toggled to:', this.isAvailable);
        
        Swal.fire({
          icon: 'success',
          title: 'Availability Updated',
          text: `You are now ${this.isAvailable ? 'available' : 'unavailable'} for assignments`,
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err) => {
        console.error('Toggle availability error:', err);
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: err.error?.message || 'Failed to update availability',
          confirmButtonColor: '#ef4444'
        });
      }
    });
  }

  private formatFieldName(field: string): string {
    return field
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }
}