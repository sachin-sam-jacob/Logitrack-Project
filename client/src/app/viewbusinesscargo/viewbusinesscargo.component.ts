import { Component, OnInit } from '@angular/core';
import { HttpService } from '../../services/http.service';
 

@Component({
  selector: 'app-view-business-cargo',
  templateUrl: './viewbusinesscargo.component.html',
  styleUrls:['./viewbusinesscargo.component.scss']
})
export class Viewbusinesscargo implements OnInit {

  cargos: any[] = [];
  keyword: string = '';

  constructor(private httpService: HttpService) {}

  ngOnInit(): void {
    this.loadCargos();
  }

  loadCargos() {
    this.httpService.getMyCargos().subscribe(res => this.cargos = res as any[]);
  }

  search() {
    if (!this.keyword) {
      this.loadCargos();
      return;
    }

    this.httpService.searchMyCargos(this.keyword)
      .subscribe(res => this.cargos = res as any[]);
  }
}