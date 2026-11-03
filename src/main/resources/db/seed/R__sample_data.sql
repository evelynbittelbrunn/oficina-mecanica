-- Sample data (dev and docker profiles). Idempotent: does not overwrite changes made through the API.
INSERT INTO services (id, name, description, base_price, estimated_minutes, active) VALUES
    ('11111111-0000-0000-0000-000000000001', 'Troca de óleo', 'Troca do óleo do motor e do filtro de óleo', 120.00, 45, TRUE),
    ('11111111-0000-0000-0000-000000000002', 'Alinhamento e balanceamento', 'Alinhamento da direção e balanceamento das quatro rodas', 150.00, 60, TRUE),
    ('11111111-0000-0000-0000-000000000003', 'Revisão de freios', 'Inspeção e substituição de pastilhas e discos quando necessário', 200.00, 90, TRUE),
    ('11111111-0000-0000-0000-000000000004', 'Diagnóstico eletrônico', 'Leitura de falhas com scanner automotivo', 90.00, 30, TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO inventory_items (id, code, name, type, unit_price, stock_quantity, minimum_stock, version) VALUES
    ('22222222-0000-0000-0000-000000000001', 'OLEO-5W30-1L', 'Óleo sintético 5W30 (1 L)', 'SUPPLY', 48.90, 40, 10, 0),
    ('22222222-0000-0000-0000-000000000002', 'FLT-OLEO-001', 'Filtro de óleo', 'PART', 35.90, 15, 5, 0),
    ('22222222-0000-0000-0000-000000000003', 'PAST-FREIO-D', 'Jogo de pastilhas de freio dianteiras', 'PART', 189.00, 6, 4, 0),
    ('22222222-0000-0000-0000-000000000004', 'FLUIDO-DOT4', 'Fluido de freio DOT 4 (500 ml)', 'SUPPLY', 32.50, 3, 5, 0)
ON CONFLICT DO NOTHING;
