import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';

declare const Swal: any;

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  itemForm!: FormGroup;

  constructor(
    public router: Router,
    public httpService: HttpService,
    private formBuilder: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.itemForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onLogin(): void {
    if (this.itemForm.invalid) {
      this.itemForm.markAllAsTouched();

      Swal.fire({
        icon: 'warning',
        title: 'Invalid Form',
        text: 'Please enter both username and password',
        confirmButtonColor: '#3085d6'
      });
      return;
    }

    this.httpService.Login(this.itemForm.value).subscribe({
      next: (data: any) => {
        if (data && data.token) {

          localStorage.setItem('role', data.role);
          this.authService.SetId(data.id);
          this.authService.SetRole(data.role);
          this.authService.saveToken(data.token);
          this.authService.setUsername(data.username);

          // ✅ NEW: Check if user has completed profile details
          this.checkProfileCompletion(data.username, data.role);

        } else {
          Swal.fire({
            icon: 'error',
            title: 'Login Failed',
            text: 'Wrong username or password',
            confirmButtonColor: '#d33'
          });
        }
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Authentication Error',
          text: 'Invalid username or password. Please try again.',
          confirmButtonColor: '#d33'
        });
      }
    });
  }

  // ✅ NEW METHOD: Check if profile is completed
  checkProfileCompletion(username: string, role: string): void {
    this.httpService.checkDetailsCompletion(username, role).subscribe({
      next: (response: any) => {
        if (response.detailsCompleted) {
          // Profile complete - go to dashboard
          Swal.fire({
            icon: 'success',
            title: 'Login Successful',
            text: `Welcome back, ${username}!`,
            timer: 1500,
            showConfirmButton: false
          }).then(() => {
            this.router.navigateByUrl('/dashboard');
          });
        } else {
          // Profile incomplete - show prompt
          Swal.fire({
            icon: 'info',
            title: 'Complete Your Profile',
            text: 'Please complete your profile to access all features',
            showCancelButton: true,
            confirmButtonText: 'Complete Now',
            cancelButtonText: 'Skip',
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#6c757d'
          }).then((result: any) => {
            if (result.isConfirmed) {
              // Redirect to user details page
              this.router.navigate(['/user-details'], {
                queryParams: {
                  username: username,
                  role: role
                }
              });
            } else {
              // Allow access but remind later
              this.router.navigateByUrl('/dashboard');
            }
          });
        }
      },
      error: () => {
        // If check fails, allow login anyway
        Swal.fire({
          icon: 'success',
          title: 'Login Successful',
          text: `Welcome back, ${username}!`,
          timer: 1500,
          showConfirmButton: false
        }).then(() => {
          this.router.navigateByUrl('/dashboard');
        });
      }
    });
  }

  registration(): void {
    this.router.navigateByUrl('/registration');
  }
}