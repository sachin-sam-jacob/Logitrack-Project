import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
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
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss']
})
export class UserDetailsComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('particleCanvas', { static: false }) canvasRef!: ElementRef<HTMLCanvasElement>;

  detailsForm!: FormGroup;
  role: string = '';
  username: string = '';
  isSubmitting = false;

  // File uploads
  licenseProofBase64: string = '';
  vehicleRcBase64: string = '';
  licenseProofName: string = '';
  vehicleRcName: string = '';

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

  ngAfterViewInit(): void {
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
      'rgba(66, 133, 244, 0.8)',
      'rgba(234, 67, 53, 0.8)',
      'rgba(251, 188, 5, 0.8)',
      'rgba(52, 168, 83, 0.8)',
      'rgba(42, 82, 152, 0.8)',
      'rgba(56, 189, 248, 0.8)'
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

    this.particles.forEach((particle, i) => {
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

      particle.x += (particle.baseX - particle.x) * 0.05;
      particle.y += (particle.baseY - particle.y) * 0.05;

      particle.x += particle.vx;
      particle.y += particle.vy;

      if (particle.x < 0 || particle.x > this.canvas.width) particle.vx *= -1;
      if (particle.y < 0 || particle.y > this.canvas.height) particle.vy *= -1;

      this.ctx.beginPath();
      this.ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2);
      this.ctx.fillStyle = particle.color;
      this.ctx.fill();

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
  // FORM METHODS
  // ============================================

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
        contactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
        location: ['', Validators.required]
      });
    } else if (this.role === 'CUSTOMER') {
      this.detailsForm = this.fb.group({
        contactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
        alternativeContactNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
        address: ['', Validators.required],
        location: ['', Validators.required]
      });
    }
  }

  onFileChange(event: any, type: string) {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        Swal.fire('File Too Large', 'Please upload a file smaller than 5MB', 'warning');
        return;
      }

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
      reader.onerror = () => {
        Swal.fire('Error', 'Failed to read file', 'error');
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit() {
    if (this.detailsForm.invalid) {
      this.detailsForm.markAllAsTouched();
      
      const errors: string[] = [];
      Object.keys(this.detailsForm.controls).forEach(key => {
        const control = this.detailsForm.get(key);
        if (control?.invalid) {
          if (control.errors?.['required']) {
            errors.push(`${this.formatFieldName(key)} is required`);
          }
          if (control.errors?.['pattern']) {
            errors.push(`${this.formatFieldName(key)} is invalid`);
          }
        }
      });

      Swal.fire({
        icon: 'warning',
        title: 'Incomplete Form',
        html: errors.join('<br>'),
        confirmButtonColor: '#3085d6'
      });
      return;
    }

    if (this.role === 'DRIVER') {
      if (!this.licenseProofBase64 || !this.vehicleRcBase64) {
        Swal.fire({
          icon: 'warning',
          title: 'Missing Documents',
          text: 'Please upload both License Proof and Vehicle RC',
          confirmButtonColor: '#3085d6'
        });
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
          title: 'Profile Completed!',
          text: 'Your profile has been set up successfully. Please login to continue.',
          timer: 2000,
          showConfirmButton: false
        }).then(() => {
          this.router.navigate(['/login']);
        });
        this.isSubmitting = false;
      },
      error: (err) => {
        console.error('Submission error:', err);
        Swal.fire({
          icon: 'error',
          title: 'Submission Failed',
          text: err.error?.message || 'Please try again',
          confirmButtonColor: '#d33'
        });
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
      cancelButtonText: 'No, Complete Now',
      confirmButtonColor: '#4285f4',
      cancelButtonColor: '#64748b'
    }).then((result: any) => {
      if (result.isConfirmed) {
        this.router.navigate(['/login']);
      }
    });
  }

  private formatFieldName(field: string): string {
    return field
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }
}