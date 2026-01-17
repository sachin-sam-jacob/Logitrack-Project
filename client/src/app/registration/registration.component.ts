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
  isSubmitting = false;

  roles: string[] = ['BUSINESS', 'DRIVER', 'CUSTOMER'];

  // OTP
  otpSent = false;
  registeredUsername = '';

  // Password rule flags
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

  // 🔥 Password strength meter
  passwordValue = '';
  strengthPercent = 0;
  strengthLabel = '';
  strengthClass = '';

  // Resend OTP
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

    // 🔴 Live password validation + strength
    this.itemForm.get('password')?.valueChanges.subscribe(value => {
      const password = value || '';
      this.passwordValue = password;

      this.hasMinLength = password.length >= 8;
      this.hasLowerCase = /[a-z]/.test(password);
      this.hasUpperCase = /[A-Z]/.test(password);
      this.hasDigit = /\d/.test(password);
      this.hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

      this.allValid =
        this.hasMinLength &&
        this.hasLowerCase &&
        this.hasUpperCase &&
        this.hasDigit &&
        this.hasSpecialChar;

      this.calculateStrength();

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

  // 🔥 Password strength calculation
  calculateStrength() {
    let score = 0;

    if (this.hasMinLength) score++;
    if (this.hasLowerCase) score++;
    if (this.hasUpperCase) score++;
    if (this.hasDigit) score++;
    if (this.hasSpecialChar) score++;

    switch (score) {
      case 0:
      case 1:
        this.strengthPercent = 20;
        this.strengthLabel = 'Weak';
        this.strengthClass = 'strength-weak';
        break;

      case 2:
      case 3:
        this.strengthPercent = 50;
        this.strengthLabel = 'Medium';
        this.strengthClass = 'strength-medium';
        break;

      case 4:
        this.strengthPercent = 75;
        this.strengthLabel = 'Strong';
        this.strengthClass = 'strength-strong';
        break;

      case 5:
        this.strengthPercent = 100;
        this.strengthLabel = 'Very Strong';
        this.strengthClass = 'strength-very-strong';
        break;
    }
  }

  // ---------------- REGISTER ----------------
  onRegister(): void {
    if (this.itemForm.invalid || this.isSubmitting) {
      this.itemForm.markAllAsTouched();
      Swal.fire('Incomplete Form', 'Please fill all fields correctly', 'warning');
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
      next: () => {
        Swal.fire(
          'OTP Sent!',
          'Check your email for the 6-digit OTP',
          'success'
        );
        this.otpSent = true;
        this.registeredUsername = payload.username;
        this.startResendTimer();
        this.isSubmitting = false;
      },
      error: (error) => {
        Swal.fire(
          'Registration Failed',
          error.error?.message || 'User already exists',
          'error'
        );
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

    this.httpService.verifyOtp({
      username: this.registeredUsername,
      otp
    }).subscribe({
      next: () => {
        Swal.fire(
          'Email Verified!',
          'You can now login',
          'success'
        ).then(() => {
          this.itemForm.reset();
          this.otpSent = false;
          this.router.navigate(['/login']);
        });
      },
      error: (error) => {
        Swal.fire(
          'Verification Failed',
          error.error?.message || 'Invalid OTP',
          'error'
        );
      }
    });
  }

  // ---------------- RESEND OTP ----------------
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

    this.httpService.resendOtp({
      username: this.registeredUsername
    }).subscribe({
      next: () => {
        Swal.fire('OTP Resent', 'Check your email', 'success');
        this.startResendTimer();
      },
      error: () => {
        Swal.fire('Error', 'Failed to resend OTP', 'error');
        this.resendDisabled = false;
      }
    });
  }
}
