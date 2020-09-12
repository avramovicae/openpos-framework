import {Component, OnInit} from '@angular/core';
import {start} from 'repl';
import {Observable} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {DialogComponent} from '../../shared/decorators/dialog-component.decorator';
import { PosScreen } from '../pos-screen/pos-screen.component';
import {DigitalReadingInterface} from './digital-reading.interface';

@DialogComponent({
    name: 'DigitalReading'
})
@Component({
    selector: 'app-digital-reading',
    templateUrl: './digital-reading.component.html',
    styleUrls: ['./digital-reading.component.scss'],
})
export class DigitalReadingComponent extends PosScreen<DigitalReadingInterface> implements OnInit{

    message$: Observable<string>;
    value$: Observable<string>;
    instructions$: Observable<string>;

    buildScreen() {
    }

    ngOnInit(): void {
        this.message$ = this.getValueUpdates<string>('DigitalReading:message');
        this.value$ = this.getValueUpdates<string>('DigitalReading:value');
        this.instructions$ = this.getValueUpdates<string>('DigitalReading:instructions');
    }
}
