package com.example.timeboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViewController {

    @GetMapping("/")
    public String index() {
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <title>TimeBoard – Démo gestion des temps</title>
                    <style>
                        body {
                            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                            background: #f4f6f9;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 900px;
                            margin: 40px auto;
                            background: #ffffff;
                            padding: 30px 40px;
                            border-radius: 16px;
                            box-shadow: 0 15px 30px rgba(0,0,0,0.08);
                        }
                        header h1 {
                            margin: 0;
                            color: #1f2933;
                            font-size: 28px;
                        }
                        .subtitle {
                            margin-top: 8px;
                            color: #6b7b93;
                        }
                        .summary-card {
                            margin-top: 24px;
                            padding: 20px;
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            background: linear-gradient(135deg, #2563eb, #1d4ed8);
                            color: white;
                            border-radius: 14px;
                        }
                        .summary-card h2 {
                            margin: 0 0 4px;
                        }
                        .summary-card p {
                            margin: 0;
                        }
                        .summary-card .badge span {
                            background: rgba(255,255,255,0.2);
                            padding: 6px 14px;
                            border-radius: 999px;
                            font-size: 12px;
                            letter-spacing: 0.08em;
                            text-transform: uppercase;
                        }
                        section h3 {
                            margin-top: 30px;
                            color: #1f2933;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 12px;
                        }
                        th, td {
                            padding: 10px 12px;
                            border-bottom: 1px solid #e1e7f0;
                            text-align: left;
                        }
                        th {
                            background: #f8fafc;
                            font-weight: 600;
                            color: #4b5563;
                        }
                        tbody tr:hover {
                            background: #f9fafb;
                        }
                        footer {
                            margin-top: 24px;
                            font-size: 13px;
                            color: #6b7b93;
                        }
                    </style>
                </head>
                <body>
                <div class="container">
                    <header>
                        <h1>TimeBoard – Démo gestion des temps</h1>
                        <p class="subtitle">Mini application Java pour démonstration CI/CD &amp; DevSecOps</p>
                    </header>

                    <section class="summary-card">
                        <div class="info">
                            <h2>Modeste (Employé chez Horoquartz)</h2>
                            <p>
                                Date : 2025-11-17<br>
                                Heures travaillées : 8 h
                            </p>
                        </div>
                        <div class="badge">
                            <span>DEMO</span>
                        </div>
                    </section>

                    <section>
                        <h3>Détail des temps</h3>
                        <table>
                            <thead>
                            <tr>
                                <th>Jour</th>
                                <th>Projet</th>
                                <th>Heures</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>Lundi</td>
                                <td>Projet e-Temptation</td>
                                <td>8</td>
                            </tr>
                            <tr>
                                <td>Mardi</td>
                                <td>Support interne</td>
                                <td>7</td>
                            </tr>
                            <tr>
                                <td>Mercredi</td>
                                <td>CI/CD &amp; DevSecOps</td>
                                <td>9</td>
                            </tr>
                            </tbody>
                        </table>
                    </section>

                    <footer>
                        <p>Healthcheck API : <a href="/health">/health</a></p>
                    </footer>
                </div>
                </body>
                </html>
                """;
    }
}
