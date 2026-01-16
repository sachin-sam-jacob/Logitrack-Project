import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';

declare var Swal: any;

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit {

  itemForm!: FormGroup;
  isSubmitting = false;  // NEW: Prevent double submit
  
  roles: string[] = ['BUSINESS', 'DRIVER', 'CUSTOMER'];

  // OTP
  otpSent = false;
  registeredUsername = '';

  // Password rules flags
  hasMinLength = false;
  hasLowerCase = false;
  hasUpperCase = false;
  hasDigit = false;
  hasSpecialChar = false;
  allValid = false;

  // Password visibility
  showPassword = false;
  showConfirmPassword = false;
  showChecklist = false;


  //resend otp
  resendDisabled = true;
  countdown = 30;
  private timer: any;

  constructor(
    private fb: FormBuilder,
    private httpService: HttpService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.itemForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(6)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      confirmPassword: ['', Validators.required],
      role: ['', Validators.required],
      otp: ['']
    }, { validators: this.passwordMatchValidator });

    // Live password validation  
    this.itemForm.get('password')?.valueChanges.subscribe(value => {
  const password = value || '';

  this.hasMinLength = password.length >= 8;
  this.hasLowerCase = /[a-z]/.test(password);
  this.hasUpperCase = /[A-Z]/.test(password);
  this.hasDigit = /\d/.test(password); // ✅ FIXED
  this.hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

  this.allValid =
    this.hasMinLength &&
    this.hasLowerCase &&
    this.hasUpperCase &&
    this.hasDigit &&
    this.hasSpecialChar;

  // ✅ auto-hide checklist when valid
  if (this.allValid) {
    this.showChecklist = false;
  }
});
  }

  // ---------------- PASSWORD ----------------
  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  hideChecklist() {
    if (this.allValid) {
      this.showChecklist = false;
    }
  }

  passwordMatchValidator(form: AbstractControl): ValidationErrors | null {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  // ---------------- REGISTER - FIXED ----------------
  onRegister(): void {
    if (this.itemForm.invalid || this.isSubmitting) {
      this.itemForm.markAllAsTouched();
      if (this.isSubmitting) {
        Swal.fire('Please wait', 'Registration in progress...', 'info');
      } else {
        Swal.fire('Incomplete Form', 'Please fill all fields correctly', 'warning');
      }
      return;
    }

    this.isSubmitting = true;

    const payload = {  
      username: this.itemForm.value.username,  
      email: this.itemForm.value.email,  
      password: this.itemForm.value.password,  
      role: this.itemForm.value.role  
    };  

    this.httpService.registerUser(payload).subscribe({  
      next: (response: any) => {
        console.log('Register response:', response);
        Swal.fire(  
          'OTP Sent!',  
          'Check your email for the 6-digit code (expires in 10 min)',  
          'success'  
        );  
        this.otpSent = true;  
        this.registeredUsername = payload.username;  
        this.startResendTimer();
        this.isSubmitting = false;
      },  
      error: (error: any) => {
        console.error('Register error:', error);
        let errorMsg = 'Registration failed. Try again.';
        if (error.error && error.error.message) {
          errorMsg = error.error.message;
        } else if (error.status === 400) {
          errorMsg = 'User already exists or invalid data';
        }
        Swal.fire('Registration Failed', errorMsg, 'error');  
        this.isSubmitting = false;
      }  
    });
  }

  // ---------------- VERIFY OTP ----------------
  verifyOtp(): void {
    const otp = this.itemForm.value.otp?.trim();

    if (!otp || otp.length !== 6) {  
      Swal.fire('Invalid OTP', 'Enter a valid 6-digit OTP', 'warning');  
      return;  
    }  

    const payload = {  
      username: this.registeredUsername,  
      otp: otp  
    };  

    this.httpService.verifyOtp(payload).subscribe({  
      next: (response: any) => {
        console.log('OTP verify response:', response);
        Swal.fire(  
          'Email Verified!',  
          'Your account has been activated. You can now login.',  
          'success'  
        ).then(() => {  
          this.itemForm.reset();  
          this.otpSent = false;
          this.isSubmitting = false;
          this.router.navigate(['/login']);  
        });  
      },
      error: (error: any) => {
        console.error('OTP verify error:', error);
        let errorMsg = 'OTP verification failed';
        if (error.error && error.error.message) {
          errorMsg = error.error.message;
        }
        Swal.fire('Verification Failed', errorMsg, 'error');
        this.isSubmitting = false;
      }  
    });
  }

  startResendTimer() {
  this.resendDisabled = true;
  this.countdown = 30;

  this.timer = setInterval(() => {
    this.countdown--;

    if (this.countdown === 0) {
      this.resendDisabled = false;
      clearInterval(this.timer);
    }
      }, 1000);
    }


    resendOtp(): void {

  this.resendDisabled = true;

  const payload = {
    username: this.registeredUsername
  };

  this.httpService.resendOtp(payload).subscribe({
    next: () => {
      Swal.fire(
        'OTP Resent',
        'A new OTP has been sent to your email',
        'success'
      );
      this.startResendTimer();
    },
    error: (error) => {
      Swal.fire(
        'Error',
        error.error?.message || 'Failed to resend OTP',
        'error'
      );
      this.resendDisabled = false;
    }
  });
}

}
