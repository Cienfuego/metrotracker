// import { Component } from '@angular/core';

// @Component({
//   selector: 'app-layout',
//   imports: [],
//   templateUrl: './layout.html',
//   styleUrl: './layout.css'
// })
// export class Layout {

// }


import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StationBoard } from '../app/station-board/station-board';
import { TrainMap} from '../app/train-map';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, StationBoard, TrainMap],
  templateUrl: './layout.html',
  styleUrls: ['./layout.css']
})
export class Layout {}
