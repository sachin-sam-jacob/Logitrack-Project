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
    if (this.role === 'DRIVER') {
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
        Swal.fire('Submission Failed', err.error?.message || 'Please try again', 'error');
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
    }).then((result:any) => {
      if (result.isConfirmed) {
        this.router.navigate(['/login']);
      }
    });
  }
}
