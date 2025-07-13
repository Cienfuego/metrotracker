// import { Component, AfterViewInit } from '@angular/core';
// import * as L from 'leaflet';
// import { HttpClient } from '@angular/common/http';
// import { CommonModule } from '@angular/common';

// @Component({
//   selector: 'app-train-map',
//   standalone: true,
//   imports: [CommonModule],
//   templateUrl: './train-map.html',
//   styleUrls: ['./train-map.css']
// })
// export class TrainMap implements AfterViewInit {

//   private map!: L.Map;

//   constructor(private http: HttpClient) {}

//   ngAfterViewInit(): void {
//     this.initMap();
//     this.loadTrainPositions();
//     setInterval(() => this.loadTrainPositions(), 10000); // Refresh every 10s
//   }

//   private initMap(): void {
//     this.map = L.map('map').setView([38.947809, -77.340217], 13); // Center at Wiehle-Reston

//     L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
//       attribution: '&copy; OpenStreetMap contributors'
//     }).addTo(this.map);
//   }

//   private loadTrainPositions(): void {
//     this.http.get<any[]>('http://localhost:8080/api/trains-near-station/N06')
//       .subscribe(trains => {
//         trains.forEach(train => {
//           const icon = L.divIcon({
//             className: 'custom-train-icon',
//             html: `<div style="transform: rotate(0deg); font-weight: bold;">🚆</div>`
//           });

//           L.marker([train.lat, train.lon], { icon })
//             .addTo(this.map)
//             .bindPopup(`To ${train.destination} (${train.car} cars)`);
//         });
//       });
//   }
// }


// import { Component, AfterViewInit } from '@angular/core';
// import * as L from 'leaflet';

// @Component({
//   selector: 'app-train-map',
//   standalone: true,
//   imports: [],
//   templateUrl: './train-map.html',
//   styleUrls: ['./train-map.css']
// })
// export class TrainMap implements AfterViewInit {
//   private map!: L.Map;

//   ngAfterViewInit(): void {
//     this.initMap();
//   }

//   private initMap(): void {
//     this.map = L.map('map').setView([38.9478, -77.3402], 14); // Center on Wiehle-Reston East

//     L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
//       maxZoom: 18,
//       attribution: '&copy; OpenStreetMap contributors'
//     }).addTo(this.map);

//     // You can later add train markers here
//     // L.marker([lat, lng]).addTo(this.map).bindPopup('Train A');
//   }
// }
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Component, AfterViewInit } from '@angular/core';
import { interval, Subscription } from 'rxjs';


import * as L from 'leaflet';

@Component({
  selector: 'app-train-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './train-map.html',
  styleUrls: ['./train-map.css']
})



export class TrainMap implements AfterViewInit {
  private map!: L.Map;
  private refreshSub!: Subscription;
  private trainMarkers: L.Layer[] = [];
  private trainLayerGroup: L.LayerGroup = L.layerGroup();

  

  constructor(private http: HttpClient) {}

  

  ngAfterViewInit(): void {
    // Delay init to ensure DOM is ready
    setTimeout(() => this.initMap(), 0);
  }
  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  private initMap(): void {
   
    this.map = L.map('map').setView([38.9478, -77.3402], 14);
    this.map.addLayer(this.trainLayerGroup);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);
    const stationIcon = L.divIcon({
      html: `
        <div style="
          display: flex;
          align-items: center;
          background: #2a2a2a;
          color: white;
          padding: 2px 6px;
          border-radius: 4px;
          font-family: Arial, sans-serif;
          font-weight: bold;
          box-shadow: 0 0 4px rgba(0,0,0,0.5);
        ">
          <span style="
            background: white;
            color: black;
            font-size: 1rem;
            font-weight: bold;
            padding: 0 4px;
            margin-right: 4px;
            border-radius: 2px;
          ">M</span> Reston
        </div>
      `,
      className: '',
      iconSize: [80, 20],
      iconAnchor: [10, 10]
    });
    L.marker([38.947809, -77.340217], { icon: stationIcon }).addTo(this.map);
    this.loadTrains();
    this.refreshSub = interval(10000).subscribe(() => this.loadTrains());
  }
  private loadTrains(): void {
    this.http.get<any[]>('http://localhost:8080/api/trains-near-station/N06')
      .subscribe(trains => {
        this.trainLayerGroup.clearLayers(); 
        trains.forEach(train => {
          const color = this.getLineColor(train.line);
          const label = `${train.destination} - ${train.etaMinutes === 0 ? 'ARR' : train.etaMinutes + ' min'}`;
          const trainIcon = L.divIcon({
            html: `
              <div style="display: flex; flex-direction: column; align-items: center;">
                <div style="
                  background: ${color};
                  border-radius: 50%;
                  width: 12px;
                  height: 12px;
                  border: 2px solid white;
      
                "></div>
                  <div style="
                  margin-top: 4px;
                  background: white;
                  color: black;
                  font-size: 0.75rem;
                  font-weight: bold;
                  padding: 2px 6px;
                  border-radius: 4px;
                  white-space: nowrap;
                ">
                  ${label}
                </div>
              </div>
            `,
            className: '',
            iconSize: [40, 24],
            iconAnchor: [6, 6]
          });
          // const marker = L.circleMarker([train.lat, train.lon], {
          //   radius: 8,
          //   color: 'blue',
          //   fillColor: '#1E90FF',
          //   fillOpacity: 0.8
          // });
          const marker = L.marker([train.lat, train.lon], { icon: trainIcon });

          marker.bindPopup(`
            <strong>Car:</strong> ${train.car}<br>
            <strong>Dest:</strong> ${train.destination}<br>
            <strong>Line:</strong> ${train.line}<br>
            <strong>ETA:</strong> ${train.etaMinutes} min
          `);
          this.trainLayerGroup.addLayer(marker);
          // marker.addTo(this.map);
          // this.trainMarkers.push(marker);
        });
      });
  }
  private getLineColor(line: string): string {
    switch (line) {
      case 'SV': return '#A2A4A1'; // Silver
      case 'RD': return '#BE1337'; // Red
      case 'BL': return '#0076A8'; // Blue
      case 'OR': return '#F7941D'; // Orange
      case 'GR': return '#00A651'; // Green
      case 'YL': return '#FFD100'; // Yellow
      default: return 'gray';
    }
  }
}

