<%@ page contentType="text/html; charset=UTF-8"%>

<style>
    body {
        height: 297mm;
        width: 210mm;
        padding: 30mm 20mm;
        /* to centre page on screen*/
        margin-left: auto;
        margin-right: auto;
        font: 14px Arial, Geneva CY, Kalimati, Geneva, sans-serif;
    }
    table {
        width: 100%;
        border-collapse: collapse;
        font: inherit;
        font-size: 100%;
    }
    td {
        border: thin solid black;
        padding: 2mm;
        vertical-align: top;
    }
    .bottom {
        display: inline-block;
        bottom: 0;
        position: absolute;
    }
    .small {
        font-size: .9em;
    }
    .no-border-top {
        border-top: none;
    }
    .no-border-bottom {
        border-bottom: none;
    }
    .va-bottom {
        vertical-align: bottom;
    }
    .in-table-cell {
        display: table-row;
    }
    .in-table-cell > * {
        display: table-cell;
    }
    .center {
        text-align: center;
    }
    .right {
        text-align: right;
    }
    .bold {
        font-weight: bold;
    }
</style>