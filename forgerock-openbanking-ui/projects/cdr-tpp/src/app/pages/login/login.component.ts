import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { ForgerockMessagesService } from '@forgerock/openbanking-ngx-common/services/forgerock-messages';
import { CDRService } from '../../services/cdr.service';

@Component({
  selector: 'app-login',
  template: `
    <mat-card>
      <mat-card-content>
        <h1>Login</h1>
        <form [formGroup]="formGroup" (ngSubmit)="onSubmit()">
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
              autocomplete="current-password"
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
              Signin
            </button>
            <p>
              Don't have an account yet?
              <a routerLink="/register" queryParamsHandling="preserve">Register</a>
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
      button[type=submit]{
        margin-bottom: 2em;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent implements OnInit {
  formGroup: FormGroup;

  constructor(
    private cdrService: CDRService,
    private messages: ForgerockMessagesService,
    private router: Router
  ) {}

  ngOnInit() {
    this.formGroup = new FormGroup({
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

    this.cdrService.login(this.formGroup.value).subscribe(
      data => {
        this.formGroup.reset();
        this.messages.success('Welcome');
        this.router.navigate(['/accounts']);
      },
      error => {
        this.formGroup.enable();
        this.messages.error(error.error.error);
      }
    );
  }
}
