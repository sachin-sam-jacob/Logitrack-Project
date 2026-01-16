import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpService } from '../../services/http.service';

declare var Swal: any;

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit {

  itemForm!: FormGroup;
  formModel: any = { role: '', email: '', password: '', username: '' };
  roles: string[] = ['Choose Role', 'BUSINESS', 'DRIVER', 'CUSTOMER'];
  hasMinLength = false;
hasLowerCase = false;
hasUpperCase = false;
hasDigit = false;
hasSpecialChar = false;
allValid = false;
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private httpService: HttpService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.itemForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(6)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      confirmPassword: ['', Validators.required],
      role: ['Choose Role', [Validators.required, this.validateRole]]
    },{validators:this.passwordMatchValidator});

  this.itemForm.get('password')?.valueChanges.subscribe(value => {
    this.hasMinLength = value.length >= 8;
    this.hasLowerCase = /[a-z]/.test(value);
    this.hasUpperCase = /[A-Z]/.test(value);
    this.hasDigit = /\d/.test(value);
    this.hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(value);

    this.allValid = this.hasMinLength && this.hasLowerCase && this.hasUpperCase && this.hasDigit && this.hasSpecialChar;
  });
}

  validateRole(control: any) {
    return control.value === 'Choose Role' ? { invalidRole: true } : null;
  }

  onRegister(): void {
    if (this.itemForm.invalid) {
      this.itemForm.markAllAsTouched();

      Swal.fire({
        icon: 'warning',
        title: 'Incomplete Form',
        text: 'Please fill all required fields correctly.',
        confirmButtonColor: '#0072ff'
      });

      return;
    }

    this.httpService.registerUser(this.itemForm.value).subscribe(
      () => {
        Swal.fire({
          icon: 'success',
          title: 'Registration Successful',
          text: 'Your account has been created successfully.',
          confirmButtonColor: '#28a745'
        }).then(() => {
          this.itemForm.reset({
            role: 'Choose Role'
          });
          this.router.navigate(['/login']);
        });
      },
      (error) => {
        Swal.fire({
          icon: 'error',
          title: 'Registration Failed',
          text: error?.error || 'Something went wrong. Please try again.',
          confirmButtonColor: '#dc3545'
        });
      }
    );
  }

showConfirmPassword = false;

toggleConfirmPasswordVisibility() {
  this.showConfirmPassword = !this.showConfirmPassword;
}

passwordMatchValidator(form: AbstractControl): ValidationErrors | null {
  const password = form.get('password')?.value;
  const confirmPassword = form.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordMismatch: true };
}

showPassword = false;
showChecklist = false;

togglePasswordVisibility() {
  this.showPassword = !this.showPassword;
}

hideChecklist() {
  // Hide checklist only if all conditions are met
  if (this.allValid) {
    this.showChecklist = false;
  }
}

}