str = input()

for offset in range(len(str)):
    bracket = ''
    if offset < len(str) // 2:
        bracket = '('
    else:
        bracket = ')'
    