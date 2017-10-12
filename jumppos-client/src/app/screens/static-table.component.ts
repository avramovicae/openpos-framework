import { Component, DoCheck } from '@angular/core';
import { SessionService } from '../session.service';
import { IScreen } from './../common/iscreen';
import { DataSource } from '@angular/cdk/collections';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';

@Component({
    selector: 'app-static-table',
    templateUrl: './static-table.component.html'
})
export class StaticTableComponent implements IScreen, DoCheck {
    private lastSequenceNum: number;
    rowData: RowDatabase;
    dataSource: RowDataSource | null;

    /** Table columns */
    columns: Array<ColumnDef> = [];
    columnIds: Array<string> = [];
    columnsById: {[key: string]: ColumnDef} = {};

    selectionMode: SelectionMode;
    selectedRow: number;
    // Name of action specified by server side which should behave like
    // a form submission
    submitActionNames: string[] = [];

    /** Prompt text to display to user */
    text: string;

    constructor(public session: SessionService) {
    }

    private initColumnDefs(): void {
        this.columns = [];
        this.columnIds = [];
        this.columnsById = {};
        // Create the list of column definitions
        let columnIdx = 0;
        if (this.session.screen.headerLabels) {
            // Initialize the list of column metadata and other convenience
            // column data structures
            this.session.screen.headerLabels.forEach(
                (headerLabel) => {
                    this.columns.push(
                        { index: columnIdx++, columnId: headerLabel, headerLabel: headerLabel }
                    );
                }
            );
            this.columnIds = this.columns.map(c => c.columnId);
            // console.log('selectionMode:' + this.selectionMode);

            // If our selection mode allows for selection of a Single table row
            // or Multiple table rows, add an extra '_selection' column to the front
            // of the list of other columnIds. The _selection column
            // will be used by the template to enable selection of a row (or rows).
            if ([SelectionMode.Single, SelectionMode.Multiple].indexOf(this.selectionMode) >= 0) {
                this.columnIds.unshift('_selection');
            }

            // console.log('columnIds:' + this.columnIds);
            this.columns.forEach((col) => { this.columnsById[col.columnId] = col; });
        }
    }

    isRowSelected(rowIndex: number) {
        return this.selectedRow === rowIndex ;
    }

    ngDoCheck(): void {
        if (this.session.screen.sequenceNumber !== this.lastSequenceNum) {
            // Screen changed, re-init
            this.init();
            this.lastSequenceNum = this.session.screen.sequenceNumber;
        }
    }

    init(): void {
        this.selectionMode = SelectionMode[this.session.screen.selectionMode as string];
        this.initColumnDefs();
        this.rowData = new RowDatabase(this.session.screen.tableData);

        this.submitActionNames = this.session.screen.submitActionNames;
        this.dataSource = new RowDataSource(this.rowData);
        this.text = this.session.screen.text;
        this.selectedRow = this.session.screen.selectedRow;
    }

    show(session: SessionService) {
    }

    onSelectRow(rowIndex: number) {
        this.selectedRow = rowIndex;
    }

    onAction(action: string) {
        if (this.submitActionNames.includes(action)) {
            this.session.response = this.selectedRow;
            this.session.onAction(action);
        }
    }
}

/**
 * Indicates if the table component will allow the user to
 * select no rows, a single row, or multiple rows.
 */
export enum SelectionMode {
    None,
    Multiple,
    Single
}

/**
 * Attributes that describe a column.
 */
export interface ColumnDef {
    /** Index of the column, 0-based */
    index: number;
    /** An identifier for the column, can just be the column header label */
    columnId: string;
    /** A label for the column that can be displayed to the user */
    headerLabel: string;

}

/**
 * Attributes that describe a row in the table to be displayed
 */
export interface TableRow {
    /** Index of the row, 0-based */
    index: number;
    /** Pre-formatted cell values to display to the user. The count of values
     * for each row must be >= the number of columns. */
    values: Array<string>;
}

/**
 * Holds the static data to be displayed. Data is provided in the form of
 * an Array of String Arrays.
 */
export class RowDatabase {
    /** Stream that emits an event whenever the data has been modified. */
    dataChange: BehaviorSubject<TableRow[]> = new BehaviorSubject<TableRow[]>([]);
    get data(): TableRow[] { return this.dataChange.value; }

    constructor(rows: Array<Array<string>>) {
        rows.forEach((row) => {
            this.addRow(row);
        });
    }

    /** Adds a new row to the database. */
    addRow(row: Array<string>) {
        const copiedData = this.data.slice();
        copiedData.push(this.createNewRow(row));
        this.dataChange.next(copiedData);
      }

    private createNewRow(row: Array<string>): TableRow {
        return {
          index: this.data.length,
          values: row
        };
    }
}

export class RowDataSource extends DataSource<any> {
    constructor(private _rowDatabase: RowDatabase) {
      super();
    }

    /** Connect function called by the table to retrieve one stream containing the data to render. */
    connect(): Observable<TableRow[]> {
      return this._rowDatabase.dataChange;
    }

    disconnect() {}
}
