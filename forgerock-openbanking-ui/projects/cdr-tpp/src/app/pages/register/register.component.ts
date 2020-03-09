import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ForgerockConfigService } from '@forgerock/openbanking-ngx-common/services/forgerock-config';
import { ForgerockMessagesService } from '@forgerock/openbanking-ngx-common/services/forgerock-messages';
import { CDRService } from '../../services/cdr.service';

@Component({
  selector: 'app-register',
  template: `
    <mat-card>
      <mat-card-content>
        <h1>Register to {{appName}} demo app</h1>
        <form [formGroup]="formGroup" (ngSubmit)="onSubmit()">
          <mat-form-field class="full-width">
            <input matInput placeholder="First Name" formControlName="firstName" required autocomplete="given-name" />
            <mat-error
              *ngIf="formGroup.controls.firstName.hasError('required') && formGroup.controls.firstName.touched"
            >
              Firstname <strong>required</strong>
            </mat-error>
          </mat-form-field>
          <mat-form-field class="full-width">
            <input matInput placeholder="Last Name" formControlName="lastName" required autocomplete="given-name" />
            <mat-error *ngIf="formGroup.controls.lastName.hasError('required') && formGroup.controls.lastName.touched">
              Lastname <strong>required</strong>
            </mat-error>
          </mat-form-field>
          <mat-form-field class="full-width">
            <input matInput placeholder="Email" formControlName="email" type="email" required autocomplete="email" />
            <mat-error *ngIf="formGroup.controls.email.hasError('required') && formGroup.controls.email.touched">
              Email <strong>required</strong>
            </mat-error>
            <mat-error *ngIf="formGroup.controls.email.hasError('email') && formGroup.controls.email.touched">
              Should be a valid email address
            </mat-error>
          </mat-form-field>
          <mat-form-field class="full-width">
            <input
              matInput
              placeholder="Password"
              type="password"
              formControlName="password"
              required
              autocomplete="new-password"
            />
            <mat-error *ngIf="formGroup.controls.password.hasError('minlength') && formGroup.controls.password.touched">
              Password must be at least 6 characters long
            </mat-error>
            <mat-error *ngIf="formGroup.controls.password.hasError('required') && formGroup.controls.password.touched">
              Password <strong>required</strong>
            </mat-error>
          </mat-form-field>
          <div fxLayout="column" fxLayoutAlign="center center">
            <button mat-flat-button type="submit" color="accent" [disabled]="formGroup.invalid">
              Signup
            </button>
            <p>
              Already have an account?
              <a routerLink="/login" queryParamsHandling="preserve">Signin</a>
            </p>
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [
    `
      :host > mat-card {
        max-width: 330px;
        display: block;
        margin: auto;
        display: flex;
        flex-direction: column;
        align-items: center;
      }

      .full-width {
        width: 100%;
      }
      button[type='submit'] {
        margin-bottom: 2em;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterComponent implements OnInit {
  formGroup: FormGroup;
  appName: '';
  constructor(
    private http: HttpClient,
    private cdrService: CDRService,
    private configService: ForgerockConfigService,
    private messages: ForgerockMessagesService,
    private router: Router
  ) {}

  ngOnInit() {
    this.appName = this.configService.get('client.name', 'MoneyWatch');
    this.formGroup = new FormGroup({
      firstName: new FormControl('', Validators.required),
      lastName: new FormControl('', Validators.required),
      email: new FormControl('', [Validators.email, Validators.required]),
      password: new FormControl('', [
        // At least 6 characters in length; Lowercase letters; Uppercase letters; Numbers; Special characters
        Validators.minLength(6),
        Validators.required
      ])
    });
  }

  onSubmit() {
    this.formGroup.disable();

    this.http.post(`${this.configService.get('nodeBackend')}/register`, this.formGroup.value).subscribe(
      data => {
        this.formGroup.reset();
        this.messages.success('Success');
        this.router.navigate(['/login']);
      },
      error => {
        this.formGroup.enable();
        this.messages.error(error.error.error);
      }
    );
  }
}
