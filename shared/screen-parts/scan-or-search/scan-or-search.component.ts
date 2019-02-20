import { Component, ViewChild, AfterViewInit, forwardRef } from '@angular/core';
import { ScreenPartComponent, ScreenPart } from '../screen-part';
import { ScanOrSearchInterface } from './scan-or-search.interface';
import { MatInput } from '@angular/material';
import { DeviceService } from '../../../core';

@ScreenPart({
    selector: 'app-scan-or-search',
    templateUrl: './scan-or-search.component.html',
    styleUrls: ['./scan-or-search.component.scss'],
    name: 'scan'
})
export class ScanOrSearchComponent extends ScreenPartComponent<ScanOrSearchInterface> implements AfterViewInit {

    @ViewChild(MatInput)
    input: MatInput;

    public barcode: string;

    constructor( public devices: DeviceService ) {
        super();
    }

    screenDataUpdated() {
        this.focusFirst();
    }

    ngAfterViewInit(): void {
        setTimeout(() => this.focusFirst());
    }

    public onEnter(): void {
        if (this.barcode && this.barcode.trim().length >= this.screenData.scanMinLength) {
        this.sessionService.onAction(this.screenData.scanActionName, this.barcode);
        this.barcode = '';
        }
    }

    private focusFirst(): void {
        if (this.screenData && this.screenData.autoFocusOnScan) {
        this.input.focus();
        }
    }

    private filterBarcodeValue(val: string): string {
        if (!val) {
        return val;
        }
        // Filter out extra characters permitted by HTML5 input type=number (for exponentials)
        const pattern = /[e|E|\+|\-|\.]/g;

        return val.toString().replace(pattern, '');
    }

    onBarcodePaste(event: ClipboardEvent) {
        const content = event.clipboardData.getData('text/plain');
        const filteredContent = this.filterBarcodeValue(content);
        if (filteredContent !== content) {
        this.log.info(`Clipboard data contains invalid characters for barcode, suppressing pasted content '${content}'`);
        }
        return filteredContent === content;
    }
}
