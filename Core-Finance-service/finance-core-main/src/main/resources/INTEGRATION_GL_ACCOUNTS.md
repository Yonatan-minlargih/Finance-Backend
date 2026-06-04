# Integration chart of accounts

Automatic GL posting (AP invoices, AP payments, AR, payroll) uses a **fixed set of account codes** defined in:

`src/main/resources/integration-gl-accounts.yml`

Manual account creation in the UI is unchanged. This file only ensures the **system-required** accounts exist and stay **active**.

## Default account codes

| Key | Code | Module | Purpose |
|-----|------|--------|---------|
| `ap-payable` | 2100 | AP | Accounts payable |
| `ap-expense-default` | 5100 | AP | Default expense (invoice lines) |
| `ap-expense-alternate` | 6100 | AP / Payroll | Alternate expense |
| `ap-vat-input` | 1150 | AP | VAT input |
| `ap-bank` | 1100 | AP / AR | Cash and bank |
| `ar-receivable` | 1200 | AR | Accounts receivable |
| `ar-revenue` | 4100 | AR | Sales revenue |
| `payroll-deductions` | 2200 | Payroll | Deductions payable |

## New environment setup

1. Set tenant ID in `.env` or `integration-gl-accounts.yml`:
   - `INTEGRATION_COA_SEED_TENANTS=your-tenant-uuid`
2. Restart Core Finance (seeds on startup when `seed-on-startup: true`).
3. Or call manually (with `X-Tenant-ID` header):
   - `POST /api/v1/accounts/seed-integration-chart`

## Fixing "Account is not active: 2100"

Run the seed endpoint or restart the service. The seeder **reactivates** existing integration accounts that were deactivated.

If seed returns **CONFLICT** for a code (e.g. `1200` used as LIABILITY but AR expects ASSET), either:

- Change the code in `integration-gl-accounts.yml` to a free code, or  
- Correct the existing COA account type in the UI.

Do not reuse integration codes for unrelated account types.
