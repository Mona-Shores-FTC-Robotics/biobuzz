{
  "version": "2.0.0",
  "timestamp": "2026-10-31T15:21:35.000Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-s-catch-1",
      "color": "#3cc8e4",
      "name": "START to S_CATCH",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 18
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-far-flower-turn-2",
      "color": "#3cc8e4",
      "name": "S_CATCH to N_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 119
      },
      "controlPoints": [
        {
          "x": 60.5,
          "y": 40
        },
        {
          "x": 60.5,
          "y": 92
        },
        {
          "x": 57.5,
          "y": 102
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.8,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 274
              }
            }
          ]
        }
      }
    },
    {
      "kind": "atomic",
      "id": "line-mjuohs00-5urnnb",
      "endPoint": {
        "x": 47.36,
        "y": 118.34
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 274,
        "endDeg": 90
      },
      "color": "#59a14f",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "name": "N_FIRE to FAR_FLOWER_TURN"
    },
    {
      "id": "to-far-flower-3",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_TURN to FAR_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 126.64
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-fire-4",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to N_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 119
      },
      "controlPoints": [
        {
          "x": 47.36,
          "y": 116.64
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 274
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-s-fire-5",
      "color": "#3cc8e4",
      "name": "N_FIRE to S_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 24
      },
      "controlPoints": [
        {
          "x": 60.5,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 40
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 274,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.1,
              "endProgress": 0.85,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.85,
              "endProgress": 0.93,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.93,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 88
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-turn-7",
      "color": "#3cc8e4",
      "name": "S_FIRE to WALL_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.16,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 88
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 88,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-8",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER_TURN to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.86,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-fire3-9",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to FIRE3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 45,
        "y": 26
      },
      "controlPoints": [
        {
          "x": 24.86,
          "y": 47.36
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 71
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 71
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-10",
      "color": "#3cc8e4",
      "name": "FIRE3 to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14,
        "y": 95
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 71,
        "endDeg": 90
      }
    }
  ],
  "shapes": [
    {
      "id": "frame-leg-red",
      "name": "HIVE frame leg (red side)",
      "vertices": [
        {
          "x": 46.6,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 90.3
        },
        {
          "x": 46.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "frame-leg-blue",
      "name": "HIVE frame leg (blue side)",
      "vertices": [
        {
          "x": 92.6,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 90.3
        },
        {
          "x": 92.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "blue-half",
      "name": "Blue half: stay out",
      "vertices": [
        {
          "x": 70.8,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 141.5
        },
        {
          "x": 70.8,
          "y": 141.5
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "to-s-catch-1"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-turn-2"
    },
    {
      "kind": "path",
      "lineId": "line-mjuohs00-5urnnb"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-3"
    },
    {
      "kind": "path",
      "lineId": "to-n-fire-4"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-5"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-7"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-8"
    },
    {
      "kind": "path",
      "lineId": "to-fire3-9"
    },
    {
      "kind": "path",
      "lineId": "to-park-10"
    }
  ],
  "fieldPoints": [],
  "robot": {
    "forwardVelocity": 75,
    "strafeVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rLength": 15.44,
    "rWidth": 16,
    "safetyMargin": 1,
    "maxVelocity": 50,
    "maxAcceleration": 45,
    "maxDeceleration": 45,
    "features": [
      {
        "name": "Front frame",
        "length": 0.56,
        "width": 16,
        "forward": 8,
        "left": 0,
        "collides": true,
        "color": "#2b2f36"
      },
      {
        "name": "FLOWER poker",
        "length": 1.5,
        "width": 1.5,
        "forward": 9.03,
        "left": 0,
        "collides": true
      }
    ]
  },
  "activePaths": [],
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "Tip",
        "RightCellUp"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "LaunchAll": 2
      }
    },
    "points": {
      "START": [
        55.6,
        7.7,
        90
      ],
      "S_CATCH": [
        57.5,
        18,
        90
      ],
      "S_FIRE": [
        55,
        24,
        88
      ],
      "N_FIRE": [
        57.5,
        119,
        274
      ],
      "PARK": [
        14,
        95,
        90
      ],
      "FAR_FLOWER": [
        47.36,
        126.64,
        90
      ],
      "FAR_FLOWER_IN": [
        47.36,
        120.84,
        90
      ],
      "FAR_FLOWER_TURN": [
        47.36,
        118.34,
        90
      ],
      "WALL_FLOWER": [
        14.86,
        47.36,
        180
      ],
      "WALL_FLOWER_IN": [
        20.66,
        47.36,
        180
      ],
      "WALL_FLOWER_TURN": [
        23.16,
        47.36,
        180
      ],
      "FIRE3": [
        45,
        26,
        71
      ]
    },
    "pathEnds": {
      "to-s-catch-1": "S_CATCH",
      "to-far-flower-turn-2": "N_FIRE",
      "to-far-flower-3": "FAR_FLOWER",
      "to-n-fire-4": "N_FIRE",
      "to-s-fire-5": "S_FIRE",
      "to-wall-flower-turn-7": "WALL_FLOWER_TURN",
      "to-wall-flower-8": "WALL_FLOWER",
      "to-fire3-9": "FIRE3",
      "to-park-10": "PARK",
      "line-mjuohs00-5urnnb": "FAR_FLOWER_TURN"
    },
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "w-2",
        "kind": "firstOf",
        "label": "Fire the preloads (TIP 1)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-3",
        "kind": "path",
        "lineId": "to-s-catch-1",
        "park": false
      },
      {
        "id": "w-6",
        "kind": "firstOf",
        "label": "TIP 1",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 4000,
            "cards": [
              {
                "id": "w-4",
                "kind": "firstOf",
                "label": "TIP 1 missed: catch",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1500,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-5",
                "kind": "firstOf",
                "label": "TIP 1 missed: fire the catch",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 3000,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              }
            ]
          }
        ]
      },
      {
        "id": "w-7",
        "kind": "firstOf",
        "label": "Catch TIP 1's spill",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-far-flower-turn-2",
        "park": false
      },
      {
        "id": "w-9",
        "kind": "firstOf",
        "label": "Fire the catch at the left CELL",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "c-mjuohs00-qftcq",
        "kind": "path",
        "lineId": "line-mjuohs00-5urnnb",
        "park": false
      },
      {
        "id": "p-10",
        "kind": "path",
        "lineId": "to-far-flower-3",
        "park": false
      },
      {
        "id": "w-11",
        "kind": "firstOf",
        "label": "The far FLOWER's 4",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-12",
        "kind": "path",
        "lineId": "to-n-fire-4",
        "park": false
      },
      {
        "id": "w-13",
        "kind": "firstOf",
        "label": "Fire the far FLOWER's 4 (TIP 2)",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 4000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-14",
        "kind": "firstOf",
        "label": "TIP 2's spill lands",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-15",
        "kind": "path",
        "lineId": "to-s-fire-5",
        "park": false
      },
      {
        "id": "w-17",
        "kind": "firstOf",
        "label": "Fire TIP 2's catch at the right CELL",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-18",
        "kind": "path",
        "lineId": "to-wall-flower-turn-7",
        "park": false
      },
      {
        "id": "p-19",
        "kind": "path",
        "lineId": "to-wall-flower-8",
        "park": false
      },
      {
        "id": "w-20",
        "kind": "firstOf",
        "label": "The wall FLOWER's 4",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-21",
        "kind": "path",
        "lineId": "to-fire3-9",
        "park": false
      },
      {
        "id": "w-22",
        "kind": "firstOf",
        "label": "Fire the wall FLOWER's 4 (TIP 3)",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-23",
        "kind": "path",
        "lineId": "to-park-10",
        "park": true
      }
    ],
    "startAt": "START",
    "exportName": "R-Quals-Locked"
  }
}
