import { Component, OnInit } from '@angular/core';
import { HttpService } from '../../services/http.service';

@Component({
  selector: 'app-view-drivers',
  templateUrl: './view-drivers.component.html',
  styleUrls: ['./view-drivers.component.scss']
})
export class ViewDriversComponent implements OnInit {

  drivers: any[] = [];
  filteredDrivers: any[] = [];
  uniqueLocations: string[] = [];
  
  searchKeyword: string = '';
  filterLocation: string = '';
  
  isLoading: boolean = true;
  showError: boolean = false;
  errorMessage: string = '';

  constructor(private httpService: HttpService) {}

  ngOnInit(): void {
    this.loadDrivers();
  }

  loadDrivers(): void {
    this.isLoading = true;
    this.httpService.getAllDrivers().subscribe({
      next: (data: any[]) => {
        this.drivers = data;
        this.filteredDrivers = data;
        this.extractUniqueLocations();
        this.isLoading = false;
      },
      error: (err:any) => {
        this.showError = true;
        this.errorMessage = 'Failed to load drivers';
        this.isLoading = false;
      }
    });
  }

  extractUniqueLocations(): void {
    const locations = new Set<string>();
    this.drivers.forEach(driver => {
      if (driver.baseLocation) {
        locations.add(driver.baseLocation);
      }
      if (driver.currentLocation) {
        locations.add(driver.currentLocation);
      }
    });
    this.uniqueLocations = Array.from(locations).sort();
  }

  filterDrivers(): void {
    this.filteredDrivers = this.drivers.filter(driver => {
      const matchesKeyword = !this.searchKeyword || 
        driver.name?.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
        driver.vehicleType?.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
        driver.baseLocation?.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
        driver.currentLocation?.toLowerCase().includes(this.searchKeyword.toLowerCase());

      const matchesLocation = !this.filterLocation || 
        driver.baseLocation === this.filterLocation ||
        driver.currentLocation === this.filterLocation;

      return matchesKeyword && matchesLocation;
    });
  }
}