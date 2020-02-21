import { trigger, transition, query, stagger, animate, style } from '@angular/animations';

const easeInOut = 'cubic-bezier(0.24, 0.06, 0.08, 1)';

export const ngForStagger = trigger('ngForStagger', [
  transition('* <=> *', [
    query(
      ':enter',
      [
        style({
          opacity: 0,
          height: 0,
          paddingTop: 0,
          paddingBottom: 0,
          transform: 'translateY(-1rem)'
        }),
        stagger('60ms', [
          animate(`.3s ${easeInOut}`, style({ height: '*', paddingTop: '*', paddingBottom: '*' })),
          animate(`.4s ${easeInOut}`, style({ opacity: 1, transform: 'translateY(0)' }))
        ])
      ],
      { optional: true }
    ),
    query(':leave', animate('50ms', style({ opacity: 0 })), {
      optional: true
    })
  ])
]);
