import { Component, OnInit, AfterViewInit, NgZone } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpService } from '../../services/http.service';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../environments/environment';

declare const Swal: any;
declare global {
  interface Window {
    handleGoogleSignIn: any;
    google: any;
  }
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, AfterViewInit {

  itemForm!: FormGroup;
  googleClientId = environment.googleClientId;
  private googleLibraryLoaded = false;

  constructor(
    public router: Router,
    public httpService: HttpService,
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private ngZone: NgZone // Add NgZone
  ) {}

  ngOnInit(): void {
    this.itemForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    // Load Google Sign-In library
    this.loadGoogleSignInScript();
  }

  ngAfterViewInit(): void {
    // Set up global callback for Google Sign-In
    window.handleGoogleSignIn = this.handleGoogleSignIn.bind(this);
  }

  // NEW: Load Google Sign-In script dynamically
  loadGoogleSignInScript(): void {
    if (this.googleLibraryLoaded) {
      this.initializeGoogleSignIn();
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.onload = () => {
      this.googleLibraryLoaded = true;
      this.initializeGoogleSignIn();
    };
    document.head.appendChild(script);
  }

  // NEW: Initialize Google Sign-In after library loads
  initializeGoogleSignIn(): void {
    setTimeout(() => {
      if (window.google) {
        window.google.accounts.id.initialize({
          client_id: this.googleClientId,
          callback: (response: any) => {
            this.ngZone.run(() => {
              this.handleGoogleSignIn(response);
            });
          }
        });

        window.google.accounts.id.renderButton(
          document.getElementById('googleSignInButton'),
          {
            theme: 'outline',
            size: 'large',
            width: '100%',
            text: 'signin_with'
          }
        );
      }
    }, 100);
  }

  // Google Sign-In callback
  handleGoogleSignIn(response: any): void {
    if (response.credential) {
      this.httpService.googleSignIn(response.credential).subscribe({
        next: (data: any) => {
          if (data && data.token) {
            localStorage.setItem('role', data.role);
            this.authService.SetId(data.id);
            this.authService.SetRole(data.role);
            this.authService.saveToken(data.token);
            this.authService.setUsername(data.username);

            Swal.fire({
              icon: 'success',
              title: 'Google Sign-In Successful',
              text: `Welcome back, ${data.username}!`,
              timer: 1500,
              showConfirmButton: false
            }).then(() => {
              // Use NgZone to ensure Angular detects the navigation
              this.ngZone.run(() => {
                if (data.role === 'ADMIN') {
                  this.router.navigateByUrl('/admin-dashboard').then(() => {
                    window.location.reload(); // Force reload for dashboard
                  });
                } else {
                  this.checkProfileCompletion(data.username, data.role);
                }
              });
            });
          }
        },
        error: (err) => {
          let errorMessage = 'Google sign-in failed';
          
          if (err.status === 404) {
            errorMessage = 'No account found with this Google email. Please register first.';
          } else if (err.status === 403) {
            errorMessage = 'Please complete email verification from registration first.';
          } else if (err.error?.message) {
            errorMessage = err.error.message;
          }

          Swal.fire({
            icon: 'error',
            title: 'Sign-In Failed',
            text: errorMessage,
            confirmButtonColor: '#d33'
          });
        }
      });
    }
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

          if (data.role === 'ADMIN') {
            Swal.fire({
              icon: 'success',
              title: 'Login Successful',
              text: `Welcome back, ${data.username}!`,
              timer: 1500,
              showConfirmButton: false
            }).then(() => {
              this.router.navigateByUrl('/admin-dashboard');
            });
          } else {
            this.checkProfileCompletion(data.username, data.role);
          }

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

  checkProfileCompletion(username: string, role: string): void {
    this.httpService.checkDetailsCompletion(username, role).subscribe({
      next: (response: any) => {
        if (response.detailsCompleted) {
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
              this.router.navigate(['/user-details'], {
                queryParams: {
                  username: username,
                  role: role
                }
              });
            } else {
              this.router.navigateByUrl('/dashboard');
            }
          });
        }
      },
      error: () => {
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