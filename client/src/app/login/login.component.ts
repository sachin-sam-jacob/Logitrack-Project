import { Component, OnInit, AfterViewInit, NgZone, ElementRef, ViewChild, OnDestroy } from '@angular/core';
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

interface Particle {
  x: number;
  y: number;
  baseX: number;
  baseY: number;
  vx: number;
  vy: number;
  size: number;
  color: string;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('particleCanvas', { static: false }) canvasRef!: ElementRef<HTMLCanvasElement>;

  itemForm!: FormGroup;
  googleClientId = environment.googleClientId;
  private googleLibraryLoaded = false;

  // Particle system variables
  private canvas!: HTMLCanvasElement;
  private ctx!: CanvasRenderingContext2D;
  private particles: Particle[] = [];
  private animationFrameId: number = 0;
  private mouse = { x: 0, y: 0 };
  private particleCount = 100;
  private connectionDistance = 150;
  private mouseRadius = 200;

  constructor(
    public router: Router,
    public httpService: HttpService,
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    this.itemForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    this.loadGoogleSignInScript();
  }

  ngAfterViewInit(): void {
    window.handleGoogleSignIn = this.handleGoogleSignIn.bind(this);
    this.initParticleSystem();
  }

  ngOnDestroy(): void {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
  }

  // ============================================
  // PARTICLE SYSTEM IMPLEMENTATION
  // ============================================

  initParticleSystem(): void {
    this.canvas = this.canvasRef.nativeElement;
    this.ctx = this.canvas.getContext('2d')!;

    this.resizeCanvas();
    this.createParticles();
    this.animate();

    // Event listeners
    window.addEventListener('resize', () => this.resizeCanvas());
    this.canvas.addEventListener('mousemove', (e) => this.onMouseMove(e));
    this.canvas.addEventListener('mouseleave', () => this.onMouseLeave());
  }

  resizeCanvas(): void {
    this.canvas.width = window.innerWidth;
    this.canvas.height = window.innerHeight;
    this.createParticles();
  }

  createParticles(): void {
    this.particles = [];
    const colors = [
      'rgba(66, 133, 244, 0.8)',   // Google Blue
      'rgba(234, 67, 53, 0.8)',    // Google Red
      'rgba(251, 188, 5, 0.8)',    // Google Yellow
      'rgba(52, 168, 83, 0.8)',    // Google Green
      'rgba(42, 82, 152, 0.8)',    // Your brand color
      'rgba(56, 189, 248, 0.8)'    // Your secondary color
    ];

    for (let i = 0; i < this.particleCount; i++) {
      const x = Math.random() * this.canvas.width;
      const y = Math.random() * this.canvas.height;

      this.particles.push({
        x: x,
        y: y,
        baseX: x,
        baseY: y,
        vx: (Math.random() - 0.5) * 0.5,
        vy: (Math.random() - 0.5) * 0.5,
        size: Math.random() * 3 + 2,
        color: colors[Math.floor(Math.random() * colors.length)]
      });
    }
  }

  onMouseMove(event: MouseEvent): void {
    const rect = this.canvas.getBoundingClientRect();
    this.mouse.x = event.clientX - rect.left;
    this.mouse.y = event.clientY - rect.top;
  }

  onMouseLeave(): void {
    this.mouse.x = -1000;
    this.mouse.y = -1000;
  }

  animate(): void {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    // Update and draw particles
    this.particles.forEach((particle, i) => {
      // Mouse interaction - repel particles
      const dx = this.mouse.x - particle.x;
      const dy = this.mouse.y - particle.y;
      const distance = Math.sqrt(dx * dx + dy * dy);

      if (distance < this.mouseRadius) {
        const angle = Math.atan2(dy, dx);
        const force = (this.mouseRadius - distance) / this.mouseRadius;
        const moveX = Math.cos(angle) * force * 8;
        const moveY = Math.sin(angle) * force * 8;

        particle.x -= moveX;
        particle.y -= moveY;
      }

      // Return to base position
      particle.x += (particle.baseX - particle.x) * 0.05;
      particle.y += (particle.baseY - particle.y) * 0.05;

      // Add gentle floating motion
      particle.x += particle.vx;
      particle.y += particle.vy;

      // Bounce off edges
      if (particle.x < 0 || particle.x > this.canvas.width) particle.vx *= -1;
      if (particle.y < 0 || particle.y > this.canvas.height) particle.vy *= -1;

      // Draw particle
      this.ctx.beginPath();
      this.ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2);
      this.ctx.fillStyle = particle.color;
      this.ctx.fill();

      // Draw connections
      for (let j = i + 1; j < this.particles.length; j++) {
        const other = this.particles[j];
        const dx2 = particle.x - other.x;
        const dy2 = particle.y - other.y;
        const dist = Math.sqrt(dx2 * dx2 + dy2 * dy2);

        if (dist < this.connectionDistance) {
          this.ctx.beginPath();
          this.ctx.strokeStyle = `rgba(66, 133, 244, ${1 - dist / this.connectionDistance})`;
          this.ctx.lineWidth = 0.5;
          this.ctx.moveTo(particle.x, particle.y);
          this.ctx.lineTo(other.x, other.y);
          this.ctx.stroke();
        }
      }
    });

    this.animationFrameId = requestAnimationFrame(() => this.animate());
  }

  // ============================================
  // GOOGLE SIGN-IN METHODS
  // ============================================

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
              this.ngZone.run(() => {
                if (data.role === 'ADMIN') {
                  this.router.navigateByUrl('/admin-dashboard').then(() => {
                    window.location.reload();
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