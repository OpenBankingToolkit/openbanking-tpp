import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-banks',
  template: `
    <app-banks-container></app-banks-container>
  `,
  styles: [
    `
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BanksComponent implements OnInit {
  ngOnInit() {}
}
