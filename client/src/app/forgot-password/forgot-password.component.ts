import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { HttpService } from '../../services/http.service';
import { Router } from '@angular/router';


declare var Swal: any;

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent {
  form: FormGroup;
  loading = false;

  successMessage = '';
  errorMessage = '';

  // OTP
  otpSent = false;
  registeredUsername = '';
  resendDisabled = true;
  countdown = 30;
  private timer: any;

  // Reset card after OTP verified
  showReset = false;

  // Password UI flags (like Registration)
  showPassword = false;
  showConfirmPassword = false;
  showChecklist = false;
  passwordValue = '';
  hasMinLength = false;
  hasLowerCase = false;
  hasUpperCase = false;
  hasDigit = false;
  hasSpecialChar = false;
  allValid = false;
  strengthPercent = 0;
  strengthLabel = '';
  strengthClass = '';

  constructor(private fb: FormBuilder, private http: HttpService,private router:Router) {
    this.form = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]],
        otp: [''],

        // reset fields (revealed after OTP verification)
        newPassword: ['', [Validators.minLength(8)]],
        confirmPassword: ['']
      },
      { validators: this.passwordMatchValidator }
    );

    // Live password checks (same logic as Registration)
    this.form.get('newPassword')?.valueChanges.subscribe((value: string) => {
      const password = value || '';

      this.passwordValue = password;
      this.hasMinLength = password.length >= 8;
      this.hasLowerCase = /[a-z]/.test(password);
      this.hasUpperCase = /[A-Z]/.test(password);
      this.hasDigit = /\d/.test(password);
      this.hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

      this.allValid =
        this.hasMinLength && this.hasLowerCase && this.hasUpperCase && this.hasDigit && this.hasSpecialChar;

      this.calculateStrength();
      if (this.allValid) this.showChecklist = false;
    });
  }

  get email()           { return this.form.get('email'); }
  get otp()             { return this.form.get('otp'); }
  get newPassword()     { return this.form.get('newPassword'); }
  get confirmPassword() { return this.form.get('confirmPassword'); }

  // Password helpers
  togglePasswordVisibility()       { this.showPassword = !this.showPassword; }
  toggleConfirmPasswordVisibility(){ this.showConfirmPassword = !this.showConfirmPassword; }
  hideChecklist()                  { if (this.allValid) this.showChecklist = false; }

  // ✅ FIX: ignore empty passwords during OTP stage so the form stays valid for Send OTP
  passwordMatchValidator = (group: AbstractControl): ValidationErrors | null => {
    const p = group.get('newPassword')?.value || '';
    const c = group.get('confirmPassword')?.value || '';

    // During OTP stage (both empty), don't block the form
    if (p.length === 0 && c.length === 0) return null;

    // When user is on reset step, enforce equality
    return p === c ? null : { passwordMismatch: true };
  };

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
        this.strengthPercent = 20; this.strengthLabel = 'Weak';        this.strengthClass = 'strength-weak'; break;
      case 2:
      case 3:
        this.strengthPercent = 50; this.strengthLabel = 'Medium';      this.strengthClass = 'strength-medium'; break;
      case 4:
        this.strengthPercent = 75; this.strengthLabel = 'Strong';      this.strengthClass = 'strength-strong'; break;
      case 5:
        this.strengthPercent = 100; this.strengthLabel = 'Very Strong'; this.strengthClass = 'strength-very-strong'; break;
    }
  }

  // Send OTP directly
  onRequestReset(): void {
    this.resetMessages();
    // ✅ Only email validity matters for this step (HTML disables by email?.invalid already)
    if (this.email?.invalid) { this.form.markAllAsTouched(); return; }

    this.loading = true;
    this.http.requestPasswordReset(this.form.value.email).subscribe({
      next: (res:any) => {
        this.loading = false;
        if (res?.username) {
          this.otpSent = true;
          this.registeredUsername = res.username;
          this.successMessage = 'OTP sent to your email.';
          this.startResendTimer();
        } else {
          this.successMessage = res?.message || 'If this email is registered, OTP has been sent.';
        }
      },
      error: (err:any) => {
        this.loading = false;
        if (err?.status === 0)       this.errorMessage = 'Cannot reach server. Check API URL/CORS.';
        else if (err?.status === 404) this.errorMessage = 'This email is not registered.';
        else this.errorMessage = err?.error?.message || 'Unable to send reset OTP. Please try again later.';
      }
    });
  }

  // Verify OTP → show reset card after OK
  onVerifyOtp(): void {
    const otp = (this.form.value.otp || '').trim();
    if (!otp || otp.length !== 6) {
      Swal.fire('Invalid OTP', 'Enter a valid 6-digit OTP', 'warning');
      return;
    }

    this.loading = true;
    this.http.verifyOtp({ username: this.registeredUsername, otp, purpose: 'RESET' }).subscribe({
      next: () => {
        this.loading = false;
        Swal.fire('OTP Verified!', 'You can now reset your password.', 'success')
          .then(() => {
            this.showReset = true;          // reveal reset card
            this.successMessage = '';
            this.errorMessage = '';
            this.form.patchValue({ otp: '' });
            clearInterval(this.timer);
            this.resendDisabled = true;
          });
      },
      error: (error) => {
        this.loading = false;
        Swal.fire('Verification Failed', error.error?.message || 'Invalid OTP', 'error');
      }
    });
  }

  // Resend OTP
  onResendOtp(): void {
    if (this.resendDisabled || !this.registeredUsername) return;

    this.resendDisabled = true;
    this.http.resendOtp({ username: this.registeredUsername }).subscribe({
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

  // Reset password API call
  onResetPassword(): void {
    this.resetMessages();

    if (!this.showReset) return;
    if (!this.allValid || this.form.hasError('passwordMismatch')) {
      this.newPassword?.markAsTouched();
      this.confirmPassword?.markAsTouched();
      Swal.fire('Invalid Password', 'Please meet all password rules and confirm correctly.', 'warning');
      return;
    }

    this.loading = true;
    const payload = { username: this.registeredUsername, newPassword: this.form.value.newPassword };

    this.http.resetPassword(payload).subscribe({
      next: () => {
        this.loading = false;
        Swal.fire('Password Updated', 'Your password has been reset successfully.', 'success')
          .then(() => {
            this.form.reset();
            this.otpSent = false;
            this.showReset = false;
            this.registeredUsername = '';
            this.router.navigate(['/login'])
          });
      },
      error: (err:any) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Unable to update password. Please try again.';
      }
    });
  }

  // utils
  private startResendTimer() {
    this.resendDisabled = true;
    this.countdown = 30;
    clearInterval(this.timer);
    this.timer = setInterval(() => {
      this.countdown--;
      if (this.countdown === 0) {
        this.resendDisabled = false;
        clearInterval(this.timer);
      }
    }, 1000);
  }

  private resetMessages() {
    this.successMessage = '';
    this.errorMessage = '';
  }
}


