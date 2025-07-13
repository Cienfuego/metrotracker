// import { Component, inject, OnInit } from '@angular/core';
// import { HttpClient } from '@angular/common/http';
// import { NgIf, NgFor } from '@angular/common';

// @Component({
//   selector: 'app',
//   standalone: true,
//   imports: [NgIf, NgFor],
//   templateUrl: './station-board.html'
// })
// export class StationBoard implements OnInit {
//   private http = inject(HttpClient);
//   trainEstimates: any[] = [];

//   ngOnInit() {
//     this.http.get<any[]>('http://localhost:8080/api/stations/N06/predictions')
//       .subscribe(data => this.trainEstimates = data);
//   }
// }

import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NgIf, NgFor } from '@angular/common';

@Component({
  selector: 'app-station-board',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './station-board.html',
  styleUrls: ['./station-board.css']
})
export class StationBoard implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  trainEstimates: any[] = [];
  private intervalId: any;

  ngOnInit() {
    this.loadPredictions();
    this.intervalId = setInterval(() => this.loadPredictions(), 10000);
  }

  ngOnDestroy() {
    clearInterval(this.intervalId);
  }

  loadPredictions() {
    this.http.get<any[]>('http://localhost:8080/api/stations/N06/predictions')
      .subscribe(data => this.trainEstimates = data);
  }
}



