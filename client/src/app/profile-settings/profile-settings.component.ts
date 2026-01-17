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

    this.checkCompletion();
    this.initForm();
  }

  checkCompletion(): void {
    this.httpService.checkDetailsCompletion(this.username, this.role).subscribe({
      next: (response: any) => {
        this.detailsCompleted = response.detailsCompleted;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
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
        contactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]]
      });
    } else if (this.role === 'CUSTOMER') {
      this.detailsForm = this.fb.group({
        contactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
        alternativeContactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
        address: ['', Validators.required]
      });
    }
  }

  onFileChange(event: any, type: string) {
    const file = event.target.files[0];
    if (file) {
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
      Swal.fire('Incomplete Form', 'Please fill all required fields', 'warning');
      return;
    }

    // Driver validation
    if (this.role === 'DRIVER' && !this.detailsCompleted) {
      if (!this.licenseProofBase64 || !this.vehicleRcBase64) {
        Swal.fire('Missing Documents', 'Please upload both License Proof and Vehicle RC', 'warning');
        return;
      }
    }

    this.isSubmitting = true;

    const payload = {
      username: this.username,
      ...this.detailsForm.value
    };

    if (this.role === 'DRIVER' && !this.detailsCompleted) {
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
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Profile Updated!',
          text: 'Your details have been saved successfully',
          timer: 2000,
          showConfirmButton: false
        }).then(() => {
          this.detailsCompleted = true;
          this.router.navigate(['/dashboard']);
        });
        this.isSubmitting = false;
      },
      error: (err) => {
        Swal.fire('Update Failed', err.error?.message || 'Please try again', 'error');
        this.isSubmitting = false;
      }
    });
  }
}