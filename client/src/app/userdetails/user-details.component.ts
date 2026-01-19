import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpService } from '../../services/http.service';

declare var Swal: any;

@Component({
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss']
})
export class UserDetailsComponent implements OnInit {
  detailsForm!: FormGroup;
  role: string = '';
  username: string = '';
  isSubmitting = false;

  // File uploads
  licenseProofBase64: string = '';
  vehicleRcBase64: string = '';
  licenseProofName: string = '';
  vehicleRcName: string = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private httpService: HttpService
  ) {}

  ngOnInit(): void {
    // Get username and role from query params
    this.route.queryParams.subscribe(params => {
      this.username = params['username'] || '';
      this.role = params['role'] || '';
    });

    this.initForm();
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
        location: ['', Validators.required] // ADDED location field
      });
    }
  }

  onFileChange(event: any, type: string) {
    const file = event.target.files[0];
    if (file) {
      // Validate file size (5MB max)
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
      reader.onerror = () => {
        Swal.fire('Error', 'Failed to read file', 'error');
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit() {
    // Check form validity
    if (this.detailsForm.invalid) {
      this.detailsForm.markAllAsTouched();
      
      // Show specific validation errors
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

    // Driver-specific validation
    if (this.role === 'DRIVER') {
      if (!this.licenseProofBase64 || !this.vehicleRcBase64) {
        Swal.fire({
          icon: 'warning',
          title: 'Missing Documents',
          text: 'Please upload both License Proof and Vehicle RC',
          confirmButtonColor: '#3085d6'
        });
        return;
      }
    }

    this.isSubmitting = true;

    const payload = {
      username: this.username,
      ...this.detailsForm.value
    };

    // Add files for driver
    if (this.role === 'DRIVER') {
      payload['licenseProof'] = this.licenseProofBase64;
      payload['vehicleRc'] = this.vehicleRcBase64;
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
      next: (res) => {
        Swal.fire({
          icon: 'success',
          title: 'Details Submitted!',
          text: 'Your profile is now complete. Please login to continue.',
          timer: 2000,
          showConfirmButton: false
        }).then(() => {
          this.router.navigate(['/login']);
        });
        this.isSubmitting = false;
      },
      error: (err) => {
        console.error('Submission error:', err);
        Swal.fire({
          icon: 'error',
          title: 'Submission Failed',
          text: err.error?.message || 'Please try again',
          confirmButtonColor: '#d33'
        });
        this.isSubmitting = false;
      }
    });
  }

  skipForNow() {
    Swal.fire({
      title: 'Skip Profile Setup?',
      text: 'You can complete your profile later from settings',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, Skip',
      cancelButtonText: 'No, Complete Now'
    }).then((result: any) => {
      if (result.isConfirmed) {
        this.router.navigate(['/login']);
      }
    });
  }

  // Helper function to format field names
  private formatFieldName(field: string): string {
    return field
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }
}