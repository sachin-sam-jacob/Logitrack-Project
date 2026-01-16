import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
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

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private httpService: HttpService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.itemForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['Choose Role', [Validators.required, this.validateRole]]
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
}