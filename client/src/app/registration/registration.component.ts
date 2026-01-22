import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, OnDestroy } from '@angular/core';
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
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('particleCanvas', { static: false }) canvasRef!: ElementRef<HTMLCanvasElement>;

  itemForm!: FormGroup;
  isSubmitting = false;

  roles: string[] = ['BUSINESS', 'DRIVER', 'CUSTOMER'];

  // OTP
  otpSent = false;
  registeredUsername = '';
  registeredRole = '';

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

  // Password strength meter
  passwordValue = '';
  strengthPercent = 0;
  strengthLabel = '';
  strengthClass = '';

  // Resend OTP
  resendDisabled = true;
  countdown = 30;
  private timer: any;

  // Particle system variables
  private canvas!: HTMLCanvasElement;
  private ctx!: CanvasRenderingContext2D;
  private particles: Particle[] = [];
  private animationFrameId: number = 0;
  private mouse = { x: 0, y: 0 };
  private particleCount = 120;
  private connectionDistance = 150;
  private mouseRadius = 200;

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

    // Live password validation + strength
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

  ngAfterViewInit(): void {
    this.initParticleSystem();
  }

  ngOnDestroy(): void {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
    if (this.timer) {
      clearInterval(this.timer);
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
  // PASSWORD METHODS
  // ============================================

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

  // ============================================
  // REGISTRATION METHODS
  // ============================================

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
        this.registeredRole = payload.role;
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
          'Please complete your profile',
          'success'
        ).then(() => {
          this.router.navigate(['/user-details'], {
            queryParams: {
              username: this.registeredUsername,
              role: this.registeredRole
            }
          });
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