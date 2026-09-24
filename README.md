# ShopperTracker

A lightweight shopping list and spending tracker that runs entirely in your
browser. No build step, no server, no dependencies — just open `index.html`.

## Features

- **Shopping list** — add items with quantity and category, tick them off as
  you shop, fill in what each one cost, and log the whole trip as purchases in
  one click.
- **Purchase log** — record purchases directly (item, price, quantity,
  category, store, date), then search and filter them by category or period.
- **Spending dashboard** — stat tiles for the last 30 days (total spent with a
  trend vs. the previous 30 days, trip count, average per trip, top category),
  a spending-by-category bar chart, and a weekly-spend column chart for the
  last 8 weeks.
- **Multi-currency** — display amounts in USD, EUR, GBP, or PLN.
- **Your data stays local** — everything is stored in `localStorage` in your
  browser. Export and import your data as JSON for backup or moving between
  devices.
- **Light and dark mode** — follows your system preference automatically.

## Getting started

Open `index.html` in any modern browser, or serve the folder:

```sh
python3 -m http.server 8000
# then visit http://localhost:8000
```

Use **Load sample data** (in the footer) to explore the dashboard with example
purchases.

## How it's built

A single self-contained HTML file: vanilla JavaScript, hand-rolled SVG charts,
and CSS custom properties for theming. State is persisted under the
`shoppertracker.v1` key in `localStorage`.

---

## MMOClases (plugin de Minecraft)

La carpeta [`mmoclases/`](mmoclases/) contiene un plugin de Paper con 4 clases RPG
(Mago, Arquero, Guerrero, Clérigo) pensado para funcionar con MMOWeaponary.
Consulta [`mmoclases/README.md`](mmoclases/README.md) y el diseño en
[`mmoclases/DISENO.md`](mmoclases/DISENO.md).
