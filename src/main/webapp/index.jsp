
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modern Hello</title>
    <style>
        /* Reset and Base Styles */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #2d3436;
        }

        /* Card Container */
        .card {
            background: rgba(255, 255, 255, 0.95);
            padding: 3rem 5rem;
            border-radius: 24px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
            text-align: center;
            transition: transform 0.3s ease;
        }

        .card:hover {
            transform: translateY(-5px);
        }

        /* Typography */
        h2 {
            font-size: 3rem;
            font-weight: 800;
            background: linear-gradient(to right, #667eea, #764ba2);
            background-clip: text;
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            letter-spacing: -1px;
        }

        p {
            margin-top: 10px;
            color: #636e72;
            font-size: 1.1rem;
        }

        .counter {
            margin-top: 1.5rem;
            font-size: 1.25rem;
            font-weight: 600;
            color: #2d3436;
        }

        .increment-btn {
            margin-top: 1rem;
            padding: 0.7rem 1.4rem;
            border: none;
            border-radius: 12px;
            background: #667eea;
            color: white;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s ease;
        }

        .increment-btn:hover {
            background: #5a6fd6;
        }
    </style>
</head>
<body>

    <div class="card">
        <h2>Hello World!</h2>
        <p>Welcome to your beautiful new interface.</p>
        <div class="counter">Count: <span id="counter-value">0</span></div>
        <button id="increment-btn" class="increment-btn" type="button">Increment</button>
    </div>

    <script>
        let count = 0;
        const counterValue = document.getElementById('counter-value');
        const incrementButton = document.getElementById('increment-btn');

        incrementButton.addEventListener('click', function () {
            count += 1;
            counterValue.textContent = count;
        });
    </script>

</body>
</html>