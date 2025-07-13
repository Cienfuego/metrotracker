// import { Routes } from '@angular/router';

// export const routes: Routes = [
//   {
//     path: '',
//     redirectTo: 'station-board',
//     pathMatch: 'full'
//   },
//   {
//     path: 'station-board',
//     loadComponent: () =>
//       import('./station-board/station-board').then(m => m.StationBoard)
//   }
// ];


import { Routes } from '@angular/router';
import { Layout } from './layout';

export const routes: Routes = [
  {
    path: '',
    component: Layout
  }
];
