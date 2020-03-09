import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AnimationItem } from 'lottie-web';
import { AnimationOptions } from 'ngx-lottie';

import { ForgerockMessagesService } from '@forgerock/openbanking-ngx-common/services/forgerock-messages';
import { CDRService } from '../../services/cdr.service';

@Component({
  selector: 'app-consent',
  template: `
    <div fxLayout="column" fxLayoutAlign="center center">
      <ng-lottie *ngIf="isLoading" [options]="options" (animationCreated)="animationCreated($event)"></ng-lottie>
    </div>
  `,
  styles: [
    `
      :host > div {
        height: 100%;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConsentComponent implements OnInit {
  public isLoading = false;
  options: AnimationOptions = {
    path: '/assets/animations/aggregating.json'
  };
  constructor(
    protected routeSnapshot: ActivatedRoute,
    private cdrService: CDRService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private message: ForgerockMessagesService
  ) {}

  ngOnInit() {
    const params = new URLSearchParams(this.routeSnapshot.snapshot.fragment);
    this.isLoading = true;
    this.cdrService
      .exchangeCode({
        code: params.get('code'),
        idToken: params.get('id_token'),
        state: params.get('state')
      })
      .subscribe(
        resp => {
          this.message.success('You are now able to query bank data');
          this.router.navigate(['/accounts']);
        },
        (error: HttpErrorResponse) => {
          console.log(error);
          this.isLoading = false;
          this.message.error(error.error.message);
          this.cdr.detectChanges();
        }
      );
  }

  animationCreated(animationItem: AnimationItem): void {
    console.log(animationItem);
  }
}
